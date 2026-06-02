package com.example.fonosshoppee.worker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadWorker extends Worker {

    private NotificationManager notificationManager;
    private static final String CHANNEL_ID = "download_channel";
    private static final int NOTIFICATION_ID = 1999;

    public DownloadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @NonNull
    @Override
    public Result doWork() {
        String downloadUrl = getInputData().getString("AUDIO_URL");
        String bookTitle = getInputData().getString("BOOK_TITLE");

        if (downloadUrl == null) return Result.failure();

        createNotificationChannel();

        try {
            URL url = new URL(downloadUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            int fileLength = connection.getContentLength();
            InputStream input = connection.getInputStream();

            File outputFile = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC), bookTitle + ".mp3");
            FileOutputStream output = new FileOutputStream(outputFile);

            byte[] data = new byte[4096];
            long total = 0;
            int count;

            // Biến này giúp ta chặn spam thông báo
            int lastProgress = -1;

            // Hiện thông báo 0% trước khi chạy vòng lặp
            notificationManager.notify(NOTIFICATION_ID, createProgressNotification(bookTitle, 0));

            while ((count = input.read(data)) != -1) {
                total += count;
                int progress = (int) (total * 100 / fileLength);

                // CHỈ cập nhật thông báo ra màn hình khi số % tăng lên (Tối đa 100 lần thay vì hàng nghìn lần)
                if (progress > lastProgress) {
                    notificationManager.notify(NOTIFICATION_ID, createProgressNotification(bookTitle, progress));
                    lastProgress = progress;
                }

                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();

            // Tải xong thì báo Hoàn tất
            notificationManager.notify(NOTIFICATION_ID + 1, createSuccessNotification(bookTitle));

            return Result.success();

        } catch (Exception e) {
            e.printStackTrace();
            // Lỗi mạng hoặc lỗi gì thì hiện thông báo lỗi
            notificationManager.notify(NOTIFICATION_ID + 2, createErrorNotification(bookTitle));
            return Result.failure();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Tải Sách", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createProgressNotification(String title, int progress) {
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle("Đang tải: " + title)
                .setContentText(progress + "%")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setProgress(100, progress, false)
                .setOngoing(true)
                .build();
    }

    private Notification createSuccessNotification(String title) {
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle("Hoàn tất!")
                .setContentText("Đã tải xong sách: " + title)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setAutoCancel(true)
                .build();
    }

    private Notification createErrorNotification(String title) {
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle("Lỗi tải xuống")
                .setContentText("Không thể tải sách: " + title)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setAutoCancel(true)
                .build();
    }
}