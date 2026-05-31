package com.example.fonosshoppee.model;

public class BookItem {
    private String title;
    private String author;
    private String coverUrl; // Link ảnh bìa

    public BookItem() {
        // Constructor rỗng cho Firebase
    }

    public BookItem(String title, String author, String coverUrl) {
        this.title = title;
        this.author = author;
        this.coverUrl = coverUrl;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCoverUrl() { return coverUrl; }
}