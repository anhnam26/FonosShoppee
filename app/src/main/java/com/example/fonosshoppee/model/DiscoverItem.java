package com.example.fonosshoppee.model;

public class DiscoverItem {
    private String title;
    private boolean fullWidth; // Firebase đọc biến boolean chuẩn nhất khi không có chữ "is" ở đầu
    private int heightDp;
    private String colorCode;
    private String imageUrl;   // Link ảnh trên mạng

    public DiscoverItem() {
        // Constructor rỗng bắt buộc cho Firebase
    }

    public DiscoverItem(String title, boolean fullWidth, int heightDp, String colorCode, String imageUrl) {
        this.title = title;
        this.fullWidth = fullWidth;
        this.heightDp = heightDp;
        this.colorCode = colorCode;
        this.imageUrl = imageUrl;
    }

    public String getTitle() { return title; }
    public boolean isFullWidth() { return fullWidth; }
    public int getHeightDp() { return heightDp; }
    public String getColorCode() { return colorCode; }
    public String getImageUrl() { return imageUrl; }
}