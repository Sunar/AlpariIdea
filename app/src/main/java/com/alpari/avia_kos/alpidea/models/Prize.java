package com.alpari.avia_kos.alpidea.models;

/**
 * Created by Avia-Kos on 10.05.16.
 */
public class Prize {

    private String name;
    private String description;
    private int point;
    private String imageUrl;

    public Prize(String name, String description, int point) {
        this.name = name;
        this.description = description;
        this.point = point;
    }

    public Prize(String name, String description, int point, String imageUrl) {
        this.name = name;
        this.description = description;
        this.point = point;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getPoint() {
        return point;
    }
}
