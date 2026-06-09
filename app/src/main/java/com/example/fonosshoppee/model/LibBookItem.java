package com.example.fonosshoppee.model;

public class LibBookItem {
    private String title;
    private String author;
    private String colorHex;
    private String coverUrl;
    private String audioUrl;
    private String localAudioPath;
    private String progressText;
    private int positionMs;
    private int durationMs;
    private long updatedAt;

    public LibBookItem() {
    }

    public LibBookItem(String title, String author, String colorHex, String coverUrl) {
        this.title = title;
        this.author = author;
        this.colorHex = colorHex;
        this.coverUrl = coverUrl;
    }

    public LibBookItem(String title, String author, String colorHex, String coverUrl, String audioUrl) {
        this.title = title;
        this.author = author;
        this.colorHex = colorHex;
        this.coverUrl = coverUrl;
        this.audioUrl = audioUrl;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getColorHex() { return colorHex; }
    public String getCoverUrl() { return coverUrl; }
    public String getAudioUrl() { return audioUrl; }
    public String getLocalAudioPath() { return localAudioPath; }
    public String getProgressText() { return progressText; }
    public int getPositionMs() { return positionMs; }
    public int getDurationMs() { return durationMs; }
    public long getUpdatedAt() { return updatedAt; }

    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }
    public void setLocalAudioPath(String localAudioPath) { this.localAudioPath = localAudioPath; }
    public void setProgressText(String progressText) { this.progressText = progressText; }
    public void setPositionMs(int positionMs) { this.positionMs = positionMs; }
    public void setDurationMs(int durationMs) { this.durationMs = durationMs; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
