package com.example.fonosshoppee.model;

public class ChallengeItem {
    private String emoji;
    private String title;
    private String progressText;
    private String actionButtonText;
    private boolean completed;

    // Constructor rỗng bắt buộc cho Firebase
    public ChallengeItem() {
    }

    public ChallengeItem(String emoji, String title, String progressText, String actionButtonText, boolean completed) {
        this.emoji = emoji;
        this.title = title;
        this.progressText = progressText;
        this.actionButtonText = actionButtonText;
        this.completed = completed;
    }

    // Các hàm Getter
    public String getEmoji() { return emoji; }
    public String getTitle() { return title; }
    public String getProgressText() { return progressText; }
    public String getActionButtonText() { return actionButtonText; }
    public boolean isCompleted() { return completed; }
}