package com.example.fonosshoppee.model;

public class BookItem {
    private String title;
    private String author;
    private String coverUrl;
    private String audioUrl; // Thêm trường lưu link âm thanh

    public BookItem() {} // Bắt buộc phải có cho Firebase

    public BookItem(String title, String author, String coverUrl, String audioUrl) {
        this.title = title;
        this.author = author;
        this.coverUrl = coverUrl;
        this.audioUrl = audioUrl;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCoverUrl() { return coverUrl; }
    public String getAudioUrl() { return audioUrl; } // Getter lấy link
}