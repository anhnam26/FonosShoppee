package com.example.fonosshoppee.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.fonosshoppee.data.UserDataStore;

import java.io.IOException;

public class AudioPlayerService extends Service {

    private static final String CHANNEL_ID = "AudioPlayerChannel";

    private MediaPlayer mediaPlayer;
    private final IBinder binder = new LocalBinder();
    private String currentAudioUrl = "";
    private String currentBookTitle = "";
    private String currentBookAuthor = "";
    private String currentBookCover = "";
    private float currentPlaybackSpeed = 1.0f;
    private OnPlayerListener listener;

    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private long lastProgressTickAt = 0;
    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && isPlaying() && currentAudioUrl != null && !currentAudioUrl.isEmpty()) {
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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "ACTION_PAUSE".equals(intent.getAction())) {
            pauseAudio();
            return START_STICKY;
        }

        if (intent != null) {
            String bookTitle = intent.getStringExtra("BOOK_TITLE");
            String bookAuthor = intent.getStringExtra("BOOK_AUTHOR");
            String bookCover = intent.getStringExtra("BOOK_COVER");

            if (bookTitle != null) currentBookTitle = bookTitle;
            if (bookAuthor != null) currentBookAuthor = bookAuthor;
            if (bookCover != null) currentBookCover = bookCover;
        }

        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Fonos Shoppee Đang Phát")
                .setContentText(currentBookTitle.isEmpty() ? "Đang phát sách nói" : currentBookTitle)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setOngoing(true)
                .build();

        startForeground(1, notification);
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
        try {
            return mediaPlayer != null && mediaPlayer.isPlaying();
        } catch (IllegalStateException e) {
            return false;
        }
    }

    public void playAudio(String url) {
        try {
            stopProgressTracking();
            currentAudioUrl = url;
            mediaPlayer.reset();
            applyAudioAttributes();
            mediaPlayer.setDataSource(url);
            mediaPlayer.setOnPreparedListener(mp -> {
                applyPlaybackSpeed(mp);
                mp.start();
                startProgressTracking();
                if (listener != null) listener.onPrepared(mp.getDuration());
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                stopProgressTracking();
                UserDataStore.saveRecentBook(
                        AudioPlayerService.this,
                        currentBookTitle,
                        currentBookAuthor,
                        currentBookCover,
                        currentAudioUrl,
                        mp.getDuration(),
                        mp.getDuration()
                );
                if (listener != null) listener.onCompletion();
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                stopProgressTracking();
                currentAudioUrl = "";
                if (listener != null) listener.onCompletion();
                return true;
            });
            mediaPlayer.prepareAsync();
        } catch (IOException | IllegalStateException e) {
            currentAudioUrl = "";
            e.printStackTrace();
        }
    }

    public void pauseAudio() {
        if (mediaPlayer != null && isPlaying()) {
            mediaPlayer.pause();
            savePlaybackProgress();
            stopProgressTracking();
        }
    }

    public void resumeAudio() {
        if (mediaPlayer != null && !isPlaying() && currentAudioUrl != null && !currentAudioUrl.isEmpty()) {
            try {
                mediaPlayer.start();
                startProgressTracking();
            } catch (IllegalStateException ignored) {
            }
        }
    }

    public void fastForward(int ms) {
        int duration = getDuration();
        int newPos = getCurrentPosition() + ms;
        seekTo(duration > 0 ? Math.min(newPos, duration) : newPos);
        savePlaybackProgress();
    }

    public void rewind(int ms) {
        int newPos = getCurrentPosition() - ms;
        seekTo(Math.max(newPos, 0));
        savePlaybackProgress();
    }

    public void setPlaybackSpeed(float speed) {
        currentPlaybackSpeed = speed;
        if (mediaPlayer != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                boolean wasPlaying = mediaPlayer.isPlaying();
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
                if (!wasPlaying) mediaPlayer.pause();
            } catch (IllegalStateException | IllegalArgumentException ignored) {
            }
        }
    }

    public int getCurrentPosition() {
        try {
            return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
        } catch (IllegalStateException e) {
            return 0;
        }
    }

    public int getDuration() {
        try {
            return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
        } catch (IllegalStateException e) {
            return 0;
        }
    }

    public void seekTo(int position) {
        try {
            if (mediaPlayer != null) mediaPlayer.seekTo(position);
            savePlaybackProgress();
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
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (IllegalStateException ignored) {
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
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

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Phát Nhạc", NotificationManager.IMPORTANCE_LOW);
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

    public interface OnPlayerListener {
        void onPrepared(int duration);
        void onCompletion();
    }
}
