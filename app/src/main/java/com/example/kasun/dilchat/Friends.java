package com.example.kasun.dilchat;

/**
 * Created by kasun on 3/14/18.
 */

class Friends {
    private String date;
    private String name;
    private String thumb;
    public Friends() {
    }
    public Friends(String date, String name, String thumb) {
        this.date = date;
        this.name = name;
        this.thumb = thumb;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }
}
