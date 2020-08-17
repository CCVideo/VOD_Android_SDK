package com.bokecc.vod.view;

public class ProgressObject {

    // 记录progressview的时间
    public int getDuration() {
        return duration;
    }

    public ProgressObject setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    int duration;
}
