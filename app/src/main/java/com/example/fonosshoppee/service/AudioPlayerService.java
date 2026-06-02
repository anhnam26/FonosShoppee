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
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;

public class AudioPlayerService extends Service {

    private MediaPlayer mediaPlayer;
    private final IBinder binder = new LocalBinder();
    private static final String CHANNEL_ID = "AudioPlayerChannel";

    // 1. BOUND SERVICE: Cho phép Activity kết nối vào đây
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

    // 2. KHỞI TẠO MEDIAPLAYER
    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
    }

    // 3. FOREGROUND SERVICE: Bật thông báo chạy ngầm
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String bookTitle = intent.getStringExtra("BOOK_TITLE");
        if (bookTitle == null) bookTitle = "Đang phát sách nói";

        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Fonos Shoppee Đang Phát")
                .setContentText(bookTitle)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setOngoing(true) // Không cho vuốt xóa khi đang phát nhạc
                .build();

        // Ép hệ thống chạy Service này ở chế độ Foreground
        startForeground(1, notification);

        return START_NOT_STICKY;
    }

    // --- CÁC HÀM ĐIỀU KHIỂN NHẠC (ACTIVITY SẼ GỌI CÁC HÀM NÀY) ---


    public void pauseAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void resumeAudio() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    // Dọn dẹp khi tắt Service
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Phát Nhạc", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    // --- CÁC HÀM HỖ TRỢ THANH SEEKBAR ---
    public int getCurrentPosition() {
        if (mediaPlayer != null) return mediaPlayer.getCurrentPosition();
        return 0;
    }

    public int getDuration() {
        if (mediaPlayer != null) return mediaPlayer.getDuration();
        return 0;
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) mediaPlayer.seekTo(position);
    }

    // Tạo "Đường dây liên lạc" báo cho màn hình biết khi nào nhạc tải xong
    public interface OnPlayerListener {
        void onPrepared(int duration);
        void onCompletion();
    }
    private OnPlayerListener listener;
    public void setListener(OnPlayerListener listener) { this.listener = listener; }

    // Sửa lại hàm playAudio một chút để nó báo tin qua listener
    public void playAudio(String url) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                mediaPlayer.start();
                if (listener != null) listener.onPrepared(mp.getDuration()); // Báo tổng độ dài
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                if (listener != null) listener.onCompletion(); // Báo khi nghe hết sách
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}