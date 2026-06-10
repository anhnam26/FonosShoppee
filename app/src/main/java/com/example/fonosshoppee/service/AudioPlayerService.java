package com.example.fonosshoppee.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;

import androidx.annotation.Nullable;

import com.example.fonosshoppee.BookDetailActivity;
import com.example.fonosshoppee.MainActivity;
import com.example.fonosshoppee.data.UserDataStore;

import java.io.IOException;

public class AudioPlayerService extends Service {

    private static final String CHANNEL_ID = "AudioPlayerChannel";
    private static final int NOTIFICATION_ID = 1;

    private static final String ACTION_PLAY = "com.example.fonosshoppee.ACTION_PLAY";
    private static final String ACTION_PAUSE = "com.example.fonosshoppee.ACTION_PAUSE";
    private static final String ACTION_TOGGLE_PLAYBACK = "com.example.fonosshoppee.ACTION_TOGGLE_PLAYBACK";
    private static final String ACTION_REWIND = "com.example.fonosshoppee.ACTION_REWIND";
    private static final String ACTION_FAST_FORWARD = "com.example.fonosshoppee.ACTION_FAST_FORWARD";

    private MediaPlayer mediaPlayer;
    private MediaSession mediaSession;
    private final IBinder binder = new LocalBinder();
    private String currentAudioUrl = "";
    private String currentBookTitle = "";
    private String currentBookAuthor = "";
    private String currentBookCover = "";
    private float currentPlaybackSpeed = 1.0f;
    private boolean isPlayerPrepared = false;
    private boolean isPreparing = false;
    private int cachedDurationMs = 0;
    private int lastKnownPositionMs = 0;
    private OnPlayerListener listener;

    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private long lastProgressTickAt = 0;
    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && isPlayerPrepared && isPlaying() && currentAudioUrl != null && !currentAudioUrl.isEmpty()) {
                long now = System.currentTimeMillis();
                long delta = lastProgressTickAt > 0 ? now - lastProgressTickAt : 0;
                lastProgressTickAt = now;

                UserDataStore.recordListening(
                        AudioPlayerService.this,
                        currentBookTitle,
                        currentBookAuthor,
                        currentBookCover,
                        currentAudioUrl,
                        getCurrentPosition(),
                        getDuration(),
                        delta
                );

                updateMediaSessionState();
                updateMediaNotification();
                progressHandler.postDelayed(this, 1000);
            }
        }
    };

    public class LocalBinder extends Binder {
        public AudioPlayerService getService() {
            return AudioPlayerService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        applyAudioAttributes();
        createMediaSession();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_PAUSE.equals(action) || "ACTION_PAUSE".equals(action)) {
                pauseAudio();
                return START_STICKY;
            } else if (ACTION_PLAY.equals(action)) {
                resumeAudio();
                return START_STICKY;
            } else if (ACTION_TOGGLE_PLAYBACK.equals(action)) {
                if (isPlaying()) pauseAudio();
                else resumeAudio();
                return START_STICKY;
            } else if (ACTION_REWIND.equals(action)) {
                rewind(15000);
                return START_STICKY;
            } else if (ACTION_FAST_FORWARD.equals(action)) {
                fastForward(30000);
                return START_STICKY;
            }

            String bookTitle = intent.getStringExtra("BOOK_TITLE");
            String bookAuthor = intent.getStringExtra("BOOK_AUTHOR");
            String bookCover = intent.getStringExtra("BOOK_COVER");

            if (bookTitle != null) currentBookTitle = bookTitle;
            if (bookAuthor != null) currentBookAuthor = bookAuthor;
            if (bookCover != null) currentBookCover = bookCover;
        }

        updateMediaMetadata();
        updateMediaSessionState();
        startForeground(NOTIFICATION_ID, buildMediaNotification());
        return START_NOT_STICKY;
    }

    public String getCurrentAudioUrl() {
        return currentAudioUrl;
    }

    public String getCurrentBookTitle() {
        return currentBookTitle;
    }

    public String getCurrentBookAuthor() {
        return currentBookAuthor;
    }

    public String getCurrentBookCover() {
        return currentBookCover;
    }

    public boolean isPlaying() {
        if (!isPlayerPrepared || mediaPlayer == null) return false;
        try {
            return mediaPlayer.isPlaying();
        } catch (IllegalStateException e) {
            return false;
        }
    }

    public void playAudio(String url) {
        if (url == null || url.trim().isEmpty()) {
            clearCurrentPlayback();
            return;
        }

        try {
            stopProgressTracking();
            currentAudioUrl = url;
            isPlayerPrepared = false;
            isPreparing = true;
            cachedDurationMs = 0;
            lastKnownPositionMs = 0;
            mediaPlayer.reset();
            applyAudioAttributes();
            mediaPlayer.setDataSource(url);
            updateMediaMetadata();
            updateMediaSessionState();
            updateMediaNotification();

            mediaPlayer.setOnPreparedListener(mp -> {
                isPreparing = false;
                isPlayerPrepared = true;
                cachedDurationMs = getPreparedDuration(mp);
                lastKnownPositionMs = 0;
                applyPlaybackSpeed(mp);
                mp.start();
                updateMediaMetadata();
                updateMediaSessionState();
                updateMediaNotification();
                startProgressTracking();
                if (listener != null) listener.onPrepared(cachedDurationMs);
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                stopProgressTracking();
                cachedDurationMs = getPreparedDuration(mp);
                lastKnownPositionMs = cachedDurationMs;
                UserDataStore.saveRecentBook(
                        AudioPlayerService.this,
                        currentBookTitle,
                        currentBookAuthor,
                        currentBookCover,
                        currentAudioUrl,
                        cachedDurationMs,
                        cachedDurationMs
                );
                updateMediaSessionState();
                updateMediaNotification();
                if (listener != null) listener.onCompletion();
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                stopProgressTracking();
                clearCurrentPlayback();
                if (listener != null) listener.onCompletion();
                return true;
            });

            mediaPlayer.prepareAsync();
        } catch (IOException | IllegalStateException e) {
            clearCurrentPlayback();
            e.printStackTrace();
        }
    }

    public void pauseAudio() {
        if (mediaPlayer != null && isPlayerPrepared && isPlaying()) {
            mediaPlayer.pause();
            savePlaybackProgress();
            stopProgressTracking();
            updateMediaSessionState();
            updateMediaNotification();
        }
    }

    public void resumeAudio() {
        if (mediaPlayer != null && isPlayerPrepared && !isPlaying() && currentAudioUrl != null && !currentAudioUrl.isEmpty()) {
            try {
                mediaPlayer.start();
                startProgressTracking();
                updateMediaSessionState();
                updateMediaNotification();
            } catch (IllegalStateException ignored) {
            }
        }
    }

    public void fastForward(int ms) {
        if (!isPlayerPrepared) return;
        int duration = getDuration();
        int newPos = getCurrentPosition() + ms;
        seekTo(duration > 0 ? Math.min(newPos, duration) : newPos);
    }

    public void rewind(int ms) {
        if (!isPlayerPrepared) return;
        int newPos = getCurrentPosition() - ms;
        seekTo(Math.max(newPos, 0));
    }

    public void setPlaybackSpeed(float speed) {
        currentPlaybackSpeed = speed;
        if (mediaPlayer != null && isPlayerPrepared && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                boolean wasPlaying = mediaPlayer.isPlaying();
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
                if (!wasPlaying) mediaPlayer.pause();
                updateMediaSessionState();
                updateMediaNotification();
            } catch (IllegalStateException | IllegalArgumentException ignored) {
            }
        }
    }

    public int getCurrentPosition() {
        if (!isPlayerPrepared || mediaPlayer == null) return lastKnownPositionMs;
        try {
            lastKnownPositionMs = mediaPlayer.getCurrentPosition();
            return lastKnownPositionMs;
        } catch (IllegalStateException e) {
            return lastKnownPositionMs;
        }
    }

    public int getDuration() {
        if (!isPlayerPrepared || mediaPlayer == null) return cachedDurationMs;
        try {
            cachedDurationMs = mediaPlayer.getDuration();
            return cachedDurationMs;
        } catch (IllegalStateException e) {
            return cachedDurationMs;
        }
    }

    public void seekTo(int position) {
        if (!isPlayerPrepared) return;
        try {
            if (mediaPlayer != null) {
                int duration = getDuration();
                int safePosition = duration > 0 ? Math.min(Math.max(position, 0), duration) : Math.max(position, 0);
                lastKnownPositionMs = safePosition;
                mediaPlayer.seekTo(safePosition);
            }
            savePlaybackProgress();
            updateMediaSessionState();
            updateMediaNotification();
        } catch (IllegalStateException ignored) {
        }
    }

    public void setListener(OnPlayerListener listener) {
        this.listener = listener;
    }

    @Override
    public void onDestroy() {
        savePlaybackProgress();
        stopProgressTracking();
        if (mediaSession != null) {
            mediaSession.setActive(false);
            mediaSession.release();
            mediaSession = null;
        }
        if (mediaPlayer != null) {
            try {
                isPlayerPrepared = false;
                isPreparing = false;
                mediaPlayer.stop();
            } catch (IllegalStateException ignored) {
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    private void createMediaSession() {
        mediaSession = new MediaSession(this, "FonosShoppeeAudio");
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() {
                resumeAudio();
            }

            @Override
            public void onPause() {
                pauseAudio();
            }

            @Override
            public void onSeekTo(long pos) {
                seekTo((int) pos);
            }

            @Override
            public void onRewind() {
                rewind(15000);
            }

            @Override
            public void onFastForward() {
                fastForward(30000);
            }
        });
        mediaSession.setActive(true);
    }

    private void applyAudioAttributes() {
        if (mediaPlayer == null) return;
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
    }

    private void applyPlaybackSpeed(MediaPlayer player) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                player.setPlaybackParams(player.getPlaybackParams().setSpeed(currentPlaybackSpeed));
            } catch (IllegalStateException | IllegalArgumentException ignored) {
            }
        }
    }

    private Notification buildMediaNotification() {
        Intent openIntent;
        if (currentAudioUrl != null && !currentAudioUrl.isEmpty()) {
            openIntent = new Intent(this, BookDetailActivity.class);
            openIntent.putExtra("BOOK_TITLE", currentBookTitle);
            openIntent.putExtra("BOOK_AUTHOR", currentBookAuthor);
            openIntent.putExtra("BOOK_COVER", currentBookCover);
            openIntent.putExtra("AUDIO_URL", currentAudioUrl);
            openIntent.putExtra("START_POSITION", getCurrentPosition());
        } else {
            openIntent = new Intent(this, MainActivity.class);
        }
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);

        builder.setSmallIcon(isPlaying() ? android.R.drawable.ic_media_play : android.R.drawable.ic_media_pause)
                .setContentTitle(currentBookTitle == null || currentBookTitle.isEmpty() ? "Fonos Shoppee" : currentBookTitle)
                .setContentText(currentBookAuthor == null || currentBookAuthor.isEmpty() ? "Đang phát sách nói" : currentBookAuthor)
                .setSubText("Fonos Shoppee")
                .setContentIntent(contentIntent)
                .setShowWhen(false)
                .setOnlyAlertOnce(true)
                .setOngoing(isPlaying())
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setCategory(Notification.CATEGORY_TRANSPORT)
                .addAction(buildAction(android.R.drawable.ic_media_rew, "Lùi 15s", ACTION_REWIND))
                .addAction(buildAction(
                        isPlaying() ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play,
                        isPlaying() ? "Tạm dừng" : "Tiếp tục",
                        ACTION_TOGGLE_PLAYBACK
                ))
                .addAction(buildAction(android.R.drawable.ic_media_ff, "Tới 30s", ACTION_FAST_FORWARD));

        int duration = getDuration();
        if (duration > 0) {
            builder.setProgress(duration, getCurrentPosition(), false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mediaSession != null) {
            builder.setStyle(new Notification.MediaStyle()
                    .setMediaSession(mediaSession.getSessionToken())
                    .setShowActionsInCompactView(0, 1, 2));
        }

        return builder.build();
    }

    private Notification.Action buildAction(int icon, String title, String action) {
        Intent intent = new Intent(this, AudioPlayerService.class);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getService(
                this,
                action.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        return new Notification.Action.Builder(icon, title, pendingIntent).build();
    }

    private void updateMediaNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, buildMediaNotification());
        }
    }

    private void updateMediaMetadata() {
        if (mediaSession == null) return;

        MediaMetadata.Builder builder = new MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, currentBookTitle != null ? currentBookTitle : "")
                .putString(MediaMetadata.METADATA_KEY_ARTIST, currentBookAuthor != null ? currentBookAuthor : "")
                .putString(MediaMetadata.METADATA_KEY_ALBUM, "Fonos Shoppee");

        int duration = getDuration();
        if (duration > 0) {
            builder.putLong(MediaMetadata.METADATA_KEY_DURATION, duration);
        }
        mediaSession.setMetadata(builder.build());
    }

    private void updateMediaSessionState() {
        if (mediaSession == null) return;

        long actions = PlaybackState.ACTION_PLAY
                | PlaybackState.ACTION_PAUSE
                | PlaybackState.ACTION_PLAY_PAUSE
                | PlaybackState.ACTION_SEEK_TO
                | PlaybackState.ACTION_REWIND
                | PlaybackState.ACTION_FAST_FORWARD;

        int state;
        if (currentAudioUrl == null || currentAudioUrl.isEmpty()) {
            state = PlaybackState.STATE_NONE;
        } else if (isPreparing) {
            state = PlaybackState.STATE_BUFFERING;
        } else if (isPlaying()) {
            state = PlaybackState.STATE_PLAYING;
        } else {
            state = PlaybackState.STATE_PAUSED;
        }

        float speed = isPlaying() ? currentPlaybackSpeed : 0f;
        PlaybackState playbackState = new PlaybackState.Builder()
                .setActions(actions)
                .setState(state, getCurrentPosition(), speed, SystemClock.elapsedRealtime())
                .build();

        mediaSession.setPlaybackState(playbackState);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Phát Nhạc",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Điều khiển phát sách nói Fonos Shoppee");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void startProgressTracking() {
        lastProgressTickAt = System.currentTimeMillis();
        progressHandler.removeCallbacks(progressRunnable);
        progressHandler.postDelayed(progressRunnable, 1000);

    }

    private void stopProgressTracking() {
        lastProgressTickAt = 0;
        progressHandler.removeCallbacks(progressRunnable);
    }

    private void savePlaybackProgress() {
        if (currentAudioUrl == null || currentAudioUrl.isEmpty()) return;
        if (!isPlayerPrepared && lastKnownPositionMs <= 0 && cachedDurationMs <= 0) return;
        UserDataStore.saveRecentBook(
                this,
                currentBookTitle,
                currentBookAuthor,
                currentBookCover,
                currentAudioUrl,
                getCurrentPosition(),
                getDuration()
        );
    }


    private int getPreparedDuration(MediaPlayer player) {
        try {
            return player != null ? player.getDuration() : 0;
        } catch (IllegalStateException e) {
            return 0;
        }
    }

    private void clearCurrentPlayback() {
        stopProgressTracking();
        currentAudioUrl = "";
        isPlayerPrepared = false;
        isPreparing = false;
        cachedDurationMs = 0;
        lastKnownPositionMs = 0;
        if (mediaPlayer != null) {
            try {
                mediaPlayer.reset();
                applyAudioAttributes();
            } catch (IllegalStateException ignored) {
            }
        }
        updateMediaSessionState();
        updateMediaNotification();
    }

    public interface OnPlayerListener {
        void onPrepared(int duration);
        void onCompletion();
    }
}
