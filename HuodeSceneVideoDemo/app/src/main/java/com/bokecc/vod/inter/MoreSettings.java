package com.bokecc.vod.inter;

public interface MoreSettings {
    void playAudioOrVideo();

    void checkNetWork();

    void downloadVideo();

    void setVideoSize(int position);

    void setSubTitle(int selectedSubtitle);

    void setBrightness(int brightness);

    void landScapeProjection();

}
