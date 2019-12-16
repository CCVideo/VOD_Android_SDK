package com.bokecc.vod.data;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class VideoPosition {
    @Id
    long id;

    String videoId;

    int position;

    public VideoPosition(String videoId, int position) {
        this.videoId = videoId;
        this.position = position;
    }

    public VideoPosition(){}

    public String getVideoId() {
        return videoId;
    }

    public int getPosition() {
        return position;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
