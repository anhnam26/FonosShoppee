package com.example.fonosshoppee;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.fonosshoppee.service.AudioPlayerService;

import java.util.Locale;

public class BookDetailActivity extends AppCompatActivity {

    private ImageView btnBack, btnShare, ivDetailCover;
    private TextView tvDetailTitle, tvDetailAuthor;
    private LinearLayout btnPlayAudio;
    private TextView tvPlayText, tvCurrentTime, tvTotalTime;
    private ImageView ivPlayIcon;
    private SeekBar seekBarAudio;
    private Button btnDownload;

    private AudioPlayerService audioService;
    private boolean isBound = false;
    private String title;
    private String audioUrl; // Biến lưu link thật của sách

    // Bộ đếm thời gian cập nhật thanh trượt
    private Handler handler = new Handler();
    private Runnable updateSeekBarRunnable = new Runnable() {
        @Override
        public void run() {
            if (isBound && audioService != null && audioService.isPlaying()) {
                int currentPos = audioService.getCurrentPosition();
                seekBarAudio.setProgress(currentPos);
                tvCurrentTime.setText(formatTime(currentPos));
            }
            handler.postDelayed(this, 1000); // Lặp lại sau mỗi 1 giây
        }
    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioPlayerService.LocalBinder binder = (AudioPlayerService.LocalBinder) service;
            audioService = binder.getService();
            isBound = true;

            // SIÊU NÂNG CẤP: Nếu vào lại đúng cuốn sách đang phát ngầm -> Tự động chạy thanh UI
            if (audioUrl != null && audioUrl.equals(audioService.getCurrentAudioUrl())) {
                if (audioService.isPlaying()) {
                    tvPlayText.setText("Tạm dừng");
                    ivPlayIcon.setImageResource(android.R.drawable.ic_media_pause);
                } else {
                    tvPlayText.setText("Tiếp tục nghe");
                    ivPlayIcon.setImageResource(android.R.drawable.ic_media_play);
                }

                // Kích hoạt lại thanh tiến trình
                int duration = audioService.getDuration();
                if (duration > 0) {
                    seekBarAudio.setMax(duration);
                    tvTotalTime.setText(formatTime(duration));
                    handler.post(updateSeekBarRunnable);
                }

                // Gắn lại sự kiện lắng nghe khi hết bài
                setupPlayerListener();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    // Hàm tạo Listener tách rời cho gọn code
    private void setupPlayerListener() {
        if (audioService != null) {
            audioService.setListener(new AudioPlayerService.OnPlayerListener() {
                @Override
                public void onPrepared(int duration) {
                    seekBarAudio.setMax(duration);
                    tvTotalTime.setText(formatTime(duration));
                    handler.post(updateSeekBarRunnable);
                }

                @Override
                public void onCompletion() {
                    handler.removeCallbacks(updateSeekBarRunnable);
                    tvPlayText.setText("Nghe chương đầu miễn phí");
                    ivPlayIcon.setImageResource(android.R.drawable.ic_media_play);
                    seekBarAudio.setProgress(0);
                    tvCurrentTime.setText("00:00");
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        // Ánh xạ
        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        ivDetailCover = findViewById(R.id.ivDetailCover);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailAuthor = findViewById(R.id.tvDetailAuthor);

        btnPlayAudio = findViewById(R.id.btnPlayAudio);
        ivPlayIcon = (ImageView) btnPlayAudio.getChildAt(0);
        tvPlayText = (TextView) btnPlayAudio.getChildAt(1);

        seekBarAudio = findViewById(R.id.seekBarAudio);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        btnDownload = findViewById(R.id.btnDownload);

        // Lấy dữ liệu
        title = getIntent().getStringExtra("BOOK_TITLE");
        String author = getIntent().getStringExtra("BOOK_AUTHOR");
        String coverUrl = getIntent().getStringExtra("BOOK_COVER");
        audioUrl = getIntent().getStringExtra("AUDIO_URL"); // Hứng link audio

        tvDetailTitle.setText(title != null ? title : "Chưa cập nhật");
        tvDetailAuthor.setText(author != null ? author : "Đang cập nhật >");
        if (coverUrl != null && !coverUrl.isEmpty()) {
            Glide.with(this).load(coverUrl).into(ivDetailCover);
        }

        Intent serviceIntent = new Intent(this, AudioPlayerService.class);
        serviceIntent.putExtra("BOOK_TITLE", title);
        ContextCompat.startForegroundService(this, serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        btnBack.setOnClickListener(v -> finish());

        btnShare.setOnClickListener(v -> {
            String shareText = "Mình đang nghe cuốn sách \"" + title + "\" trên Fonos Shoppee. Rất hay, bạn tải app nghe thử nhé!";
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Chia sẻ sách qua:"));
        });

        // --- TÍNH NĂNG TUA NHẠC ---
        seekBarAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isBound && audioService != null) {
                    audioService.seekTo(progress);
                    tvCurrentTime.setText(formatTime(progress));
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // --- PHÁT NHẠC TỪ API THẬT ---
        // --- PHÁT NHẠC THÔNG MINH (PHÂN BIỆT BÀI MỚI/CŨ) ---
        btnPlayAudio.setOnClickListener(v -> {
            if (isBound && audioService != null) {

                // So sánh xem đang bấm vào sách cũ hay sách mới
                String playingUrl = audioService.getCurrentAudioUrl();
                boolean isSameBook = audioUrl != null && audioUrl.equals(playingUrl);

                if (isSameBook) {
                    // TRƯỜNG HỢP 1: ĐANG Ở ĐÚNG CUỐN SÁCH ĐÓ -> BẬT/TẮT NHƯ BÌNH THƯỜNG
                    if (audioService.isPlaying()) {
                        audioService.pauseAudio();
                        tvPlayText.setText("Tiếp tục nghe");
                        ivPlayIcon.setImageResource(android.R.drawable.ic_media_play);
                    } else {
                        audioService.resumeAudio();
                        tvPlayText.setText("Tạm dừng");
                        ivPlayIcon.setImageResource(android.R.drawable.ic_media_pause);
                    }
                } else {
                    // TRƯỜNG HỢP 2: BẤM SANG CUỐN SÁCH MỚI HOÀN TOÀN -> ÉP CHẠY LẠI TỪ ĐẦU
                    if (audioUrl == null || audioUrl.isEmpty()) {
                        Toast.makeText(this, "Sách này chưa có bản Audio!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Toast.makeText(this, "Đang chuyển sách, đợi một lát nhé...", Toast.LENGTH_SHORT).show();

                    // Gửi link mới sang Service (Service sẽ tự động ngắt bài cũ và load bài mới)
                    audioService.playAudio(audioUrl);

                    tvPlayText.setText("Tạm dừng");
                    ivPlayIcon.setImageResource(android.R.drawable.ic_media_pause);

                    // Xóa thời gian bài cũ trên màn hình, đợi bài mới load xong
                    seekBarAudio.setProgress(0);
                    tvCurrentTime.setText("00:00");

                    setupPlayerListener();
                }
            }
        });

        // --- TẢI SÁCH VỀ MÁY TỪ API THẬT ---
        btnDownload.setOnClickListener(v -> {
            if (audioUrl == null || audioUrl.isEmpty()) {
                Toast.makeText(this, "Sách này chưa có bản Audio để tải!", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Bắt đầu tải file. Vui lòng kéo thanh thông báo xuống để xem!", Toast.LENGTH_SHORT).show();

            androidx.work.Data inputData = new androidx.work.Data.Builder()
                    .putString("AUDIO_URL", audioUrl)
                    .putString("BOOK_TITLE", title != null ? title : "Sach_Audio")
                    .build();

            androidx.work.OneTimeWorkRequest downloadRequest = new androidx.work.OneTimeWorkRequest.Builder(com.example.fonosshoppee.worker.DownloadWorker.class)
                    .setInputData(inputData)
                    .build();

            androidx.work.WorkManager.getInstance(this).enqueue(downloadRequest);
        });
    }

    // Hàm chuyển đổi mili-giây sang định dạng phút:giây (VD: 03:45)
    // Hàm chuyển đổi mili-giây sang định dạng giờ:phút:giây (VD: 02:05:30 hoặc 45:15)
    private String formatTime(int milliseconds) {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / (1000 * 60)) % 60;
        int hours = (milliseconds / (1000 * 60 * 60)); // Tính tổng số giờ

        // Nếu âm thanh dài hơn 1 tiếng, hiển thị định dạng HH:MM:SS
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        }
        // Nếu dưới 1 tiếng, chỉ hiển thị MM:SS cho gọn
        else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            handler.removeCallbacks(updateSeekBarRunnable); // Tắt bộ đếm để không tốn pin
            unbindService(serviceConnection);
            isBound = false;
        }
    }
}