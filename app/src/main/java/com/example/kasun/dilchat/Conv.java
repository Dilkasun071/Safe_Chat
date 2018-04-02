package com.example.kasun.dilchat;

/**
 * Created by kasun on 3/24/18.
 */

public class Conv {
    public boolean seen;
    public long timestamp;

    public Conv(boolean seen, long timestamp) {
        this.seen = seen;
        this.timestamp = timestamp;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Conv() {

    }
}
