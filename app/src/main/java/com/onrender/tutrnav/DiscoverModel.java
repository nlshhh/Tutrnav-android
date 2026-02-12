package com.onrender.tutrnav;

public class DiscoverModel {
    private String title;
    private String subtitle;
    private int imageRes; // Using drawable resource ID for now

    public DiscoverModel(String title, String subtitle, int imageRes) {
        this.title = title;
        this.subtitle = subtitle;
        this.imageRes = imageRes;
    }

    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public int getImageRes() { return imageRes; }
}