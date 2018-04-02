package com.example.kasun.dilchat;

/**
 * Created by Kasun on 3/10/2018.
 */

class Users {
    public String name;
    public String status;
    public String image;
    public String thumb;

    public Users(String name, String status, String image, String thumb) {
        this.name = name;
        this.status = status;
        this.image = image;
        this.thumb = thumb;
    }

    public Users() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }



}
