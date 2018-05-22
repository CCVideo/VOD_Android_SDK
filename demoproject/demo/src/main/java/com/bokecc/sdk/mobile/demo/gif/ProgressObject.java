package com.bokecc.sdk.mobile.demo.gif;

/**
 * Created by liufh on 2017/9/27.
 */

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
