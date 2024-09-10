package com.example.omnitrace;

public class ImageItem {
    private int imageResource;
    private String description;

    public ImageItem(int imageResource, String description) {
        this.imageResource = imageResource;
        this.description = description;
    }

    public int getImageResource() {
        return imageResource;
    }

    public String getDescription() {
        return description;
    }
}
