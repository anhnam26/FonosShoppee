package com.example.fonosshoppee.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.text.TextUtils;

import com.example.fonosshoppee.model.LibBookItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class UserDataStore {

    private static final String USER_PREF_PREFIX = "fonos_user_";
    private static final String DEVICE_PREF = "fonos_device_library";

    private static final String KEY_LAST_LOGIN_DATE = "last_login_date";
    private static final String KEY_LOGIN_STREAK = "login_streak";
    private static final String KEY_MONTH_LOGIN_DAYS_PREFIX = "login_days_";
    private static final String KEY_DAY_LISTEN_PREFIX = "listen_day_";
    private static final String KEY_MONTH_LISTEN_PREFIX = "listen_month_";
    private static final String KEY_RECENT_BOOKS = "recent_books";
    private static final String KEY_DOWNLOADED_BOOKS = "downloaded_books";

    private static final int MAX_RECENT_BOOKS = 30;

    private UserDataStore() {
    }

    public static void recordAppOpened(Context context) {
        if (context == null) return;

        SharedPreferences prefs = userPrefs(context);
        String today = todayKey();
        String lastLoginDate = prefs.getString(KEY_LAST_LOGIN_DATE, "");
        if (today.equals(lastLoginDate)) return;

        int currentStreak = prefs.getInt(KEY_LOGIN_STREAK, 0);
        int newStreak = yesterdayKey().equals(lastLoginDate) ? currentStreak + 1 : 1;

        String monthKey = monthKey();
        Set<String> loginDays = new HashSet<>(
                prefs.getStringSet(KEY_MONTH_LOGIN_DAYS_PREFIX + monthKey, new HashSet<>())
        );
        loginDays.add(today);

        prefs.edit()
                .putString(KEY_LAST_LOGIN_DATE, today)
                .putInt(KEY_LOGIN_STREAK, newStreak)
                .putStringSet(KEY_MONTH_LOGIN_DAYS_PREFIX + monthKey, loginDays)
                .apply();
    }

    public static ChallengeStats getChallengeStats(Context context) {
        if (context == null) return new ChallengeStats(false, 0, 0, 0, 0);

        SharedPreferences prefs = userPrefs(context);
        String today = todayKey();
        String monthKey = monthKey();
        boolean loggedInToday = today.equals(prefs.getString(KEY_LAST_LOGIN_DATE, ""));
        long dailyListenMs = prefs.getLong(KEY_DAY_LISTEN_PREFIX + today, 0);
        long monthlyListenMs = prefs.getLong(KEY_MONTH_LISTEN_PREFIX + monthKey, 0);
        Set<String> loginDays = prefs.getStringSet(KEY_MONTH_LOGIN_DAYS_PREFIX + monthKey, new HashSet<>());
        int streak = prefs.getInt(KEY_LOGIN_STREAK, 0);

        return new ChallengeStats(
                loggedInToday,
                dailyListenMs,
                monthlyListenMs,
                loginDays != null ? loginDays.size() : 0,
                streak
        );
    }

    public static void recordListening(
            Context context,
            String title,
            String author,
            String coverUrl,
            String audioUrl,
            int positionMs,
            int durationMs,
            long listenedMs
    ) {
        if (context == null) return;

        long safeListenedMs = Math.max(0, Math.min(listenedMs, 5000));
        if (safeListenedMs > 0) {
            SharedPreferences prefs = userPrefs(context);
            String today = todayKey();
            String monthKey = monthKey();
            long dayTotal = prefs.getLong(KEY_DAY_LISTEN_PREFIX + today, 0) + safeListenedMs;
            long monthTotal = prefs.getLong(KEY_MONTH_LISTEN_PREFIX + monthKey, 0) + safeListenedMs;
            prefs.edit()
                    .putLong(KEY_DAY_LISTEN_PREFIX + today, dayTotal)
                    .putLong(KEY_MONTH_LISTEN_PREFIX + monthKey, monthTotal)
                    .apply();
        }

        saveRecentBook(context, title, author, coverUrl, audioUrl, positionMs, durationMs);
    }

    public static void saveRecentBook(
            Context context,
            String title,
            String author,
            String coverUrl,
            String audioUrl,
            int positionMs,
            int durationMs
    ) {
        if (context == null || (isBlank(title) && isBlank(audioUrl))) return;

        String localAudioPath = isLocalFile(audioUrl) ? audioUrl : "";
        SharedPreferences prefs = userPrefs(context);
        JSONArray current = readArray(prefs, KEY_RECENT_BOOKS);
        JSONArray next = new JSONArray();

        JSONObject fresh = new JSONObject();
        try {
            fresh.put("title", valueOrDefault(title, "Sách đang nghe"));
            fresh.put("author", valueOrDefault(author, ""));
            fresh.put("coverUrl", valueOrDefault(coverUrl, ""));
            fresh.put("audioUrl", valueOrDefault(audioUrl, ""));
            fresh.put("localAudioPath", localAudioPath);
            fresh.put("positionMs", Math.max(0, positionMs));
            fresh.put("durationMs", Math.max(0, durationMs));
            fresh.put("updatedAt", System.currentTimeMillis());
            next.put(fresh);

            int count = 1;
            for (int i = 0; i < current.length() && count < MAX_RECENT_BOOKS; i++) {
                JSONObject old = current.optJSONObject(i);
                if (old == null) continue;
                if (sameBook(title, audioUrl, localAudioPath, old)) continue;
                next.put(old);
                count++;
            }

            prefs.edit().putString(KEY_RECENT_BOOKS, next.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    public static List<LibBookItem> getRecentBooks(Context context) {
        List<LibBookItem> books = new ArrayList<>();
        if (context == null) return books;

        JSONArray array = readArray(userPrefs(context), KEY_RECENT_BOOKS);
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.optJSONObject(i);
            if (obj == null) continue;

            String audioUrl = obj.optString("audioUrl", "");
            String localAudioPath = obj.optString("localAudioPath", "");
            if (isLocalFile(audioUrl) && !new File(audioUrl).exists()) continue;

            LibBookItem item = new LibBookItem(
                    obj.optString("title", "Sách đang nghe"),
                    obj.optString("author", ""),
                    "",
                    obj.optString("coverUrl", ""),
                    audioUrl
            );
            item.setLocalAudioPath(!isBlank(localAudioPath) ? localAudioPath : (isLocalFile(audioUrl) ? audioUrl : ""));
            item.setPositionMs(obj.optInt("positionMs", 0));
            item.setDurationMs(obj.optInt("durationMs", 0));
            item.setUpdatedAt(obj.optLong("updatedAt", 0));
            item.setProgressText(buildProgressText(item.getPositionMs(), item.getDurationMs()));
            books.add(item);
        }
        return books;
    }

    public static void saveDownloadedBook(
            Context context,
            String title,
            String author,
            String coverUrl,
            String remoteAudioUrl,
            String localAudioPath
    ) {
        if (context == null || isBlank(localAudioPath)) return;

        SharedPreferences prefs = devicePrefs(context);
        JSONArray current = readArray(prefs, KEY_DOWNLOADED_BOOKS);
        JSONArray next = new JSONArray();

        JSONObject fresh = new JSONObject();
        try {
            fresh.put("title", valueOrDefault(title, "Sách đã tải"));
            fresh.put("author", valueOrDefault(author, ""));
            fresh.put("coverUrl", valueOrDefault(coverUrl, ""));
            fresh.put("audioUrl", valueOrDefault(remoteAudioUrl, ""));
            fresh.put("localAudioPath", localAudioPath);
            fresh.put("downloadedAt", System.currentTimeMillis());
            next.put(fresh);

            int count = 1;
            for (int i = 0; i < current.length(); i++) {
                JSONObject old = current.optJSONObject(i);
                if (old == null) continue;
                if (sameDownload(title, remoteAudioUrl, localAudioPath, old)) continue;
                next.put(old);
                count++;
            }

            prefs.edit().putString(KEY_DOWNLOADED_BOOKS, next.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    public static List<LibBookItem> getDownloadedBooks(Context context) {
        List<LibBookItem> books = new ArrayList<>();
        if (context == null) return books;

        SharedPreferences prefs = devicePrefs(context);
        JSONArray array = readArray(prefs, KEY_DOWNLOADED_BOOKS);
        JSONArray cleaned = new JSONArray();
        Set<String> knownLocalPaths = new HashSet<>();
        boolean changed = false;

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.optJSONObject(i);
            if (obj == null) continue;

            String localPath = obj.optString("localAudioPath", "");
            if (isBlank(localPath) || !new File(localPath).exists()) {
                changed = true;
                continue;
            }

            cleaned.put(obj);
            knownLocalPaths.add(localPath);

            String title = obj.optString("title", "Sách đã tải");
            String remoteAudioUrl = obj.optString("audioUrl", "");
            ProgressInfo progressInfo = findRecentProgress(context, title, remoteAudioUrl, localPath);

            LibBookItem item = new LibBookItem(
                    title,
                    obj.optString("author", ""),
                    "",
                    obj.optString("coverUrl", ""),
                    remoteAudioUrl
            );
            item.setLocalAudioPath(localPath);
            item.setPositionMs(progressInfo.positionMs);
            item.setDurationMs(progressInfo.durationMs);
            item.setUpdatedAt(obj.optLong("downloadedAt", 0));
            item.setProgressText(progressInfo.positionMs > 0
                    ? buildProgressText(progressInfo.positionMs, progressInfo.durationMs)
                    : "Đã tải xuống");
            books.add(item);
        }

        addLooseDownloadedFiles(context, books, knownLocalPaths);

        if (changed) {
            prefs.edit().putString(KEY_DOWNLOADED_BOOKS, cleaned.toString()).apply();
        }
        return books;
    }

    public static void removeDownloadedBook(Context context, LibBookItem item, boolean deleteFile) {
        if (context == null || item == null) return;

        String localPath = item.getLocalAudioPath();
        if (deleteFile && !isBlank(localPath)) {
            File file = new File(localPath);
            if (file.exists()) file.delete();
        }

        SharedPreferences prefs = devicePrefs(context);
        JSONArray current = readArray(prefs, KEY_DOWNLOADED_BOOKS);
        JSONArray next = new JSONArray();
        for (int i = 0; i < current.length(); i++) {
            JSONObject old = current.optJSONObject(i);
            if (old == null) continue;
            if (sameDownload(item.getTitle(), item.getAudioUrl(), localPath, old)) continue;
            next.put(old);
        }
        prefs.edit().putString(KEY_DOWNLOADED_BOOKS, next.toString()).apply();
    }

    public static final class ChallengeStats {
        public final boolean loggedInToday;
        public final long dailyListenMs;
        public final long monthlyListenMs;
        public final int monthlyLoginDays;
        public final int loginStreakDays;

        private ChallengeStats(
                boolean loggedInToday,
                long dailyListenMs,
                long monthlyListenMs,
                int monthlyLoginDays,
                int loginStreakDays
        ) {
            this.loggedInToday = loggedInToday;
            this.dailyListenMs = dailyListenMs;
            this.monthlyListenMs = monthlyListenMs;
            this.monthlyLoginDays = monthlyLoginDays;
            this.loginStreakDays = loginStreakDays;
        }
    }

    private static ProgressInfo findRecentProgress(Context context, String title, String remoteAudioUrl, String localAudioPath) {
        JSONArray recentBooks = readArray(userPrefs(context), KEY_RECENT_BOOKS);
        for (int i = 0; i < recentBooks.length(); i++) {
            JSONObject obj = recentBooks.optJSONObject(i);
            if (obj != null && sameBook(title, remoteAudioUrl, localAudioPath, obj)) {
                return new ProgressInfo(obj.optInt("positionMs", 0), obj.optInt("durationMs", 0));
            }
        }
        return new ProgressInfo(0, 0);
    }

    private static void addLooseDownloadedFiles(Context context, List<LibBookItem> books, Set<String> knownLocalPaths) {
        File musicDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        scanLooseDownloadedDir(context, musicDir, books, knownLocalPaths);
        scanLooseDownloadedDir(context, context.getFilesDir(), books, knownLocalPaths);
    }

    private static void scanLooseDownloadedDir(
            Context context,
            File dir,
            List<LibBookItem> books,
            Set<String> knownLocalPaths
    ) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) return;

        File[] files = dir.listFiles((file, name) -> name != null && name.toLowerCase(Locale.US).endsWith(".mp3"));
        if (files == null) return;

        for (File file : files) {
            String localPath = file.getAbsolutePath();
            if (knownLocalPaths.contains(localPath)) continue;

            String title = file.getName().replaceFirst("\\.mp3$", "");
            ProgressInfo progressInfo = findRecentProgress(context, title, "", localPath);
            LibBookItem item = new LibBookItem(title, "", "", "", "");
            item.setLocalAudioPath(localPath);
            item.setPositionMs(progressInfo.positionMs);
            item.setDurationMs(progressInfo.durationMs);
            item.setProgressText(progressInfo.positionMs > 0
                    ? buildProgressText(progressInfo.positionMs, progressInfo.durationMs)
                    : "Đã tải xuống");
            books.add(item);
            knownLocalPaths.add(localPath);
        }
    }

    private static final class ProgressInfo {
        final int positionMs;
        final int durationMs;

        ProgressInfo(int positionMs, int durationMs) {
            this.positionMs = positionMs;
            this.durationMs = durationMs;
        }
    }

    private static SharedPreferences userPrefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(USER_PREF_PREFIX + currentUserKey(), Context.MODE_PRIVATE);
    }

    private static SharedPreferences devicePrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(DEVICE_PREF, Context.MODE_PRIVATE);
    }

    private static String currentUserKey() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user != null ? user.getUid() : "local_user";
        return uid.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }

    private static JSONArray readArray(SharedPreferences prefs, String key) {
        try {
            return new JSONArray(prefs.getString(key, "[]"));
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    private static boolean sameBook(String title, String audioUrl, String localAudioPath, JSONObject obj) {
        String oldTitle = obj.optString("title", "");
        String oldAudio = obj.optString("audioUrl", "");
        String oldLocalPath = obj.optString("localAudioPath", "");

        if (!isBlank(title) && title.equalsIgnoreCase(oldTitle)) return true;
        if (!isBlank(audioUrl) && audioUrl.equals(oldAudio)) return true;
        return !isBlank(localAudioPath) && localAudioPath.equals(oldLocalPath);
    }

    private static boolean sameDownload(String title, String audioUrl, String localAudioPath, JSONObject obj) {
        String oldTitle = obj.optString("title", "");
        String oldAudio = obj.optString("audioUrl", "");
        String oldLocalPath = obj.optString("localAudioPath", "");

        if (!isBlank(localAudioPath) && localAudioPath.equals(oldLocalPath)) return true;
        if (!isBlank(audioUrl) && audioUrl.equals(oldAudio)) return true;
        return !isBlank(title) && title.equalsIgnoreCase(oldTitle);
    }

    private static boolean isLocalFile(String path) {
        if (isBlank(path)) return false;
        return !(path.startsWith("http://") || path.startsWith("https://") || path.startsWith("content://"));
    }

    private static String buildProgressText(int positionMs, int durationMs) {
        if (positionMs <= 0) return "Chưa nghe";
        if (durationMs > 0) return "Đã nghe " + formatTime(positionMs) + " / " + formatTime(durationMs);
        return "Đã nghe tới " + formatTime(positionMs);
    }

    private static String formatTime(int milliseconds) {
        int totalSeconds = Math.max(0, milliseconds / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private static String valueOrDefault(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private static boolean isBlank(String value) {
        return TextUtils.isEmpty(value) || value.trim().isEmpty();
    }

    private static String todayKey() {
        return dateKey(System.currentTimeMillis());
    }

    private static String yesterdayKey() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        return dateKey(calendar.getTimeInMillis());
    }

    private static String dateKey(long timeMillis) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(timeMillis));
    }

    private static String monthKey() {
        return new SimpleDateFormat("yyyy-MM", Locale.US).format(new Date());
    }
}
