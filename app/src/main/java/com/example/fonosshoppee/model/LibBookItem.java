package com.example.fonosshoppee.model;

public class LibBookItem {
    // Các biến này PHẢI GIỐNG HỆT tên trường (Field) bạn đã tạo trên Firebase
    private String title;
    private String author;
    private String colorHex;
    private String coverUrl;

    // 1. Constructor rỗng (BẮT BUỘC phải có để Firebase đọc được dữ liệu)
    public LibBookItem() {
    }

    // 2. Constructor có tham số
    public LibBookItem(String title, String author, String colorHex, String coverUrl) {
        this.title = title;
        this.author = author;
        this.colorHex = colorHex;
        this.coverUrl = coverUrl;
    }

    // 3. Các hàm Getter (BẮT BUỘC phải có để hiển thị dữ liệu ra giao diện)
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getColorHex() { return colorHex; }
    public String getCoverUrl() { return coverUrl; }
}