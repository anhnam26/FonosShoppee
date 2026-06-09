package com.example.fonosshoppee;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.fonosshoppee.data.UserDataStore;
import com.example.fonosshoppee.receiver.SleepTimerReceiver;
import com.example.fonosshoppee.service.AudioPlayerService;

import java.util.Locale;

public class BookDetailActivity extends AppCompatActivity {

    private LinearLayout btnBack, btnShare;
    private ImageView ivDetailCover, ivPlayIcon;
    private TextView tvDetailTitle, tvDetailAuthor, tvCurrentTime, tvTotalTime;
    private CardView btnPlayAudio, layoutSpeedVertical;
    private SeekBar seekBarAudio, seekBarSpeedVertical;
    private Button btnDownload, btnSleepTimer, btnSpeed;
    private ImageView btnRewind15, btnForward30, btnPrevBook, btnNextBook;

    private AudioPlayerService audioService;
    private boolean isBound = false;
    private String title, author, coverUrl, audioUrl;
    private int startPositionMs = 0;
    private int pendingSeekPositionMs = 0;
    private boolean autoPlay = false;
    private boolean autoPlayHandled = false;

    private final Handler handler = new Handler();
    private final Runnable updateSeekBarRunnable = new Runnable() {
        @Override
        public void run() {
            syncPlayerProgress();
            handler.postDelayed(this, 1000);
        }
    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioPlayerService.LocalBinder binder = (AudioPlayerService.LocalBinder) service;
            audioService = binder.getService();
            isBound = true;
            setupPlayerListener();

            if (isCurrentBookLoaded()) {
                if (autoPlay && !autoPlayHandled) {
                    int duration = audioService.getDuration();
                    int seekTo = clampStartPosition(startPositionMs, duration);
                    if (seekTo > 0) audioService.seekTo(seekTo);
                    if (!audioService.isPlaying()) audioService.resumeAudio();
                    autoPlayHandled = true;
                }
                syncPlayerProgress();
                ivPlayIcon.setImageResource(audioService.isPlaying()
                        ? android.R.drawable.ic_media_pause
                        : android.R.drawable.ic_media_play);
                handler.post(updateSeekBarRunnable);
            } else if (autoPlay && !autoPlayHandled) {
                startSelectedAudio(startPositionMs);
                autoPlayHandled = true;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        bindViews();
        readIntentData();
        renderBookInfo();
        startAndBindAudioService();
        setupActions();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        ivDetailCover = findViewById(R.id.ivDetailCover);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailAuthor = findViewById(R.id.tvDetailAuthor);
        btnPlayAudio = findViewById(R.id.btnPlayAudio);
        ivPlayIcon = findViewById(R.id.ivPlayIcon);
        seekBarAudio = findViewById(R.id.seekBarAudio);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        btnDownload = findViewById(R.id.btnDownload);
        btnRewind15 = findViewById(R.id.btnRewind15);
        btnForward30 = findViewById(R.id.btnForward30);
        btnPrevBook = findViewById(R.id.btnPrevBook);
        btnNextBook = findViewById(R.id.btnNextBook);
        btnSleepTimer = findViewById(R.id.btnSleepTimer);
        btnSpeed = findViewById(R.id.btnSpeed);
        layoutSpeedVertical = findViewById(R.id.layoutSpeedVertical);
        seekBarSpeedVertical = findViewById(R.id.seekBarSpeedVertical);
    }

    private void readIntentData() {
        title = getIntent().getStringExtra("BOOK_TITLE");
        author = getIntent().getStringExtra("BOOK_AUTHOR");
        coverUrl = getIntent().getStringExtra("BOOK_COVER");
        audioUrl = getIntent().getStringExtra("AUDIO_URL");
        startPositionMs = Math.max(0, getIntent().getIntExtra("START_POSITION", 0));
        pendingSeekPositionMs = startPositionMs;
        autoPlay = getIntent().getBooleanExtra("AUTO_PLAY", false);
    }

    private void renderBookInfo() {
        tvDetailTitle.setText(title != null ? title : "Chưa cập nhật");
        tvDetailAuthor.setText(author != null ? author : "Đang cập nhật");
        if (coverUrl != null && !coverUrl.isEmpty()) {
            Glide.with(this).load(coverUrl).into(ivDetailCover);
        }
    }

    private void startAndBindAudioService() {
        Intent serviceIntent = new Intent(this, AudioPlayerService.class);
        serviceIntent.putExtra("BOOK_TITLE", title);
        serviceIntent.putExtra("BOOK_AUTHOR", author);
        serviceIntent.putExtra("BOOK_COVER", coverUrl);
        ContextCompat.startForegroundService(this, serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> finish());
        btnShare.setOnClickListener(v -> shareBook());
        btnSleepTimer.setOnClickListener(v -> showSleepTimerMenu());

        seekBarAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isBound && audioService != null && isCurrentBookLoaded()) {
                    audioService.seekTo(progress);
                    tvCurrentTime.setText(formatTime(progress));
                    saveCurrentProgress();
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnRewind15.setOnClickListener(v -> {
            if (isBound && audioService != null && isCurrentBookLoaded()) {
                audioService.rewind(15000);
                saveCurrentProgress();
            }
        });

        btnForward30.setOnClickListener(v -> {
            if (isBound && audioService != null && isCurrentBookLoaded()) {
                audioService.fastForward(30000);
                saveCurrentProgress();
            }
        });

        btnPrevBook.setOnClickListener(v -> Toast.makeText(this, "Tính năng cần danh sách sách", Toast.LENGTH_SHORT).show());
        btnNextBook.setOnClickListener(v -> Toast.makeText(this, "Tính năng cần danh sách sách", Toast.LENGTH_SHORT).show());

        btnSpeed.setOnClickListener(v -> layoutSpeedVertical.setVisibility(
                layoutSpeedVertical.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
        ));

        seekBarSpeedVertical.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float speed = 0.5f + (progress / 10.0f);
                btnSpeed.setText(String.format(Locale.getDefault(), "Tốc độ: %.1fx", speed));
                if (isBound && audioService != null) audioService.setPlaybackSpeed(speed);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnPlayAudio.setOnClickListener(v -> togglePlayback());
        btnDownload.setOnClickListener(v -> enqueueDownload());
    }

    private void setupPlayerListener() {
        if (audioService == null) return;

        audioService.setListener(new AudioPlayerService.OnPlayerListener() {
            @Override
            public void onPrepared(int duration) {
                seekBarAudio.setMax(duration);
                tvTotalTime.setText(formatTime(duration));
                int seekTo = clampStartPosition(pendingSeekPositionMs, duration);
                if (seekTo > 0) {
                    audioService.seekTo(seekTo);
                    seekBarAudio.setProgress(seekTo);
                    tvCurrentTime.setText(formatTime(seekTo));
                }
                pendingSeekPositionMs = 0;
                handler.post(updateSeekBarRunnable);
            }

            @Override
            public void onCompletion() {
                handler.removeCallbacks(updateSeekBarRunnable);
                ivPlayIcon.setImageResource(android.R.drawable.ic_media_play);
                seekBarAudio.setProgress(0);
                tvCurrentTime.setText("00:00");
                int duration = audioService.getDuration();
                UserDataStore.saveRecentBook(BookDetailActivity.this, title, author, coverUrl, audioUrl, duration, duration);
            }
        });
    }

    private void togglePlayback() {
        if (!isBound || audioService == null) return;

        if (isCurrentBookLoaded()) {
            if (audioService.isPlaying()) {
                audioService.pauseAudio();
                ivPlayIcon.setImageResource(android.R.drawable.ic_media_play);
                saveCurrentProgress();
            } else {
                audioService.resumeAudio();
                ivPlayIcon.setImageResource(android.R.drawable.ic_media_pause);
                handler.post(updateSeekBarRunnable);
            }
        } else {
            startSelectedAudio(startPositionMs);
        }
    }

    private void startSelectedAudio(int seekPositionMs) {
        if (audioUrl == null || audioUrl.isEmpty()) {
            Toast.makeText(this, "Sách này chưa có audio", Toast.LENGTH_SHORT).show();
            return;
        }

        pendingSeekPositionMs = seekPositionMs;
        setupPlayerListener();
        audioService.playAudio(audioUrl);
        ivPlayIcon.setImageResource(android.R.drawable.ic_media_pause);
        seekBarAudio.setProgress(0);
        tvCurrentTime.setText("00:00");
        seekBarSpeedVertical.setProgress(5);
        btnSpeed.setText("Tốc độ: 1.0x");
    }

    private void syncPlayerProgress() {
        if (!isBound || audioService == null || !isCurrentBookLoaded()) return;

        int currentPos = audioService.getCurrentPosition();
        int duration = audioService.getDuration();
        if (duration > 0) {
            seekBarAudio.setMax(duration);
            tvTotalTime.setText(formatTime(duration));
        }
        seekBarAudio.setProgress(currentPos);
        tvCurrentTime.setText(formatTime(currentPos));
        ivPlayIcon.setImageResource(audioService.isPlaying()
                ? android.R.drawable.ic_media_pause
                : android.R.drawable.ic_media_play);

        if (audioService.isPlaying()) {
            UserDataStore.saveRecentBook(this, title, author, coverUrl, audioUrl, currentPos, duration);
        } else {
            if (currentPos > 0) saveCurrentProgress();
        }
    }

    private void saveCurrentProgress() {
        if (!isBound || audioService == null || !isCurrentBookLoaded()) return;
        UserDataStore.saveRecentBook(
                this,
                title,
                author,
                coverUrl,
                audioUrl,
                audioService.getCurrentPosition(),
                audioService.getDuration()
        );
    }

    private void enqueueDownload() {
        if (audioUrl == null || audioUrl.isEmpty()) {
            Toast.makeText(this, "Sách này chưa có audio", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isLocalAudio(audioUrl)) {
            Toast.makeText(this, "Sách này đã có trên máy", Toast.LENGTH_SHORT).show();
            return;
        }

        androidx.work.Data inputData = new androidx.work.Data.Builder()
                .putString("AUDIO_URL", audioUrl)
                .putString("BOOK_TITLE", title != null ? title : "Sach")
                .putString("BOOK_AUTHOR", author != null ? author : "")
                .putString("BOOK_COVER", coverUrl != null ? coverUrl : "")
                .build();
        androidx.work.OneTimeWorkRequest downloadRequest =
                new androidx.work.OneTimeWorkRequest.Builder(com.example.fonosshoppee.worker.DownloadWorker.class)
                        .setInputData(inputData)
                        .build();
        androidx.work.WorkManager.getInstance(this).enqueue(downloadRequest);
        Toast.makeText(this, "Đang tải sách để nghe offline", Toast.LENGTH_SHORT).show();
    }

    private void shareBook() {
        String shareText = "Mình đang nghe cuốn sách \"" + (title != null ? title : "") + "\" trên Fonos Shoppee.";
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Chia sẻ sách qua:"));
    }

    private boolean isCurrentBookLoaded() {
        return audioUrl != null && audioService != null && audioUrl.equals(audioService.getCurrentAudioUrl());
    }

    private boolean isLocalAudio(String value) {
        return value != null
                && !value.startsWith("http://")
                && !value.startsWith("https://")
                && !value.startsWith("content://");
    }

    private int clampStartPosition(int positionMs, int durationMs) {
        if (positionMs <= 0) return 0;
        if (durationMs <= 0) return positionMs;
        if (positionMs >= durationMs - 2000) return 0;
        return Math.min(positionMs, durationMs);
    }

    private void showSleepTimerMenu() {
        PopupMenu popup = new PopupMenu(this, btnSleepTimer);
        popup.getMenu().add("Không hẹn giờ");
        popup.getMenu().add("15 phút");
        popup.getMenu().add("30 phút");
        popup.getMenu().add("45 phút");
        popup.getMenu().add("1 giờ");
        popup.getMenu().add("2 giờ");
        popup.getMenu().add("3 giờ");
        popup.getMenu().add("5 giờ");

        popup.setOnMenuItemClickListener(item -> {
            String selected = item.getTitle().toString();
            btnSleepTimer.setText(selected);
            if (selected.equals("Không hẹn giờ")) {
                cancelSleepTimer();
                Toast.makeText(this, "Đã hủy hẹn giờ", Toast.LENGTH_SHORT).show();
            } else {
                long minutes = parseMinutes(selected);
                startSleepTimer(minutes);
            }
            return true;
        });
        popup.show();
    }

    private long parseMinutes(String text) {
        if (text.startsWith("15")) return 15;
        if (text.startsWith("30")) return 30;
        if (text.startsWith("45")) return 45;
        if (text.startsWith("1")) return 60;
        if (text.startsWith("2")) return 120;
        if (text.startsWith("3")) return 180;
        if (text.startsWith("5")) return 300;
        return 0;
    }

    private void startSleepTimer(long minutes) {
        try {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, SleepTimerReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            long triggerTime = System.currentTimeMillis() + (minutes * 60 * 1000);
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            Toast.makeText(this, "Sách sẽ tự tắt sau " + minutes + " phút nữa", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi hẹn giờ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelSleepTimer() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, SleepTimerReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }

    private String formatTime(int milliseconds) {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / (1000 * 60)) % 60;
        int hours = milliseconds / (1000 * 60 * 60);
        return hours > 0
                ? String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
                : String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onPause() {
        saveCurrentProgress();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        saveCurrentProgress();
        handler.removeCallbacks(updateSeekBarRunnable);
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
        super.onDestroy();
    }
}
