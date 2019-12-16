package com.bokecc.vod.data;

import android.os.Parcel;
import android.os.Parcelable;


public class HuodeVideoInfo implements Parcelable {
    private String videoTitle;
    private String videoId;
    private String videoTime;
    private String videoCover;
    private boolean isSelected;
    private boolean isSelectedDownload;
    private boolean isShowSelectButton;

    public HuodeVideoInfo(String videoTitle, String videoId) {
        this.videoTitle = videoTitle;
        this.videoId = videoId;
    }

    public HuodeVideoInfo(String videoTitle, String videoId, String videoTime, String videoCover) {
        this.videoTitle = videoTitle;
        this.videoId = videoId;
        this.videoTime = videoTime;
        this.videoCover = videoCover;
    }

    public HuodeVideoInfo(String videoCover, String videoTitle, String videoId, boolean isSelected) {
        this.videoCover = videoCover;
        this.videoTitle = videoTitle;
        this.videoId = videoId;
        this.isSelected = isSelected;
    }

    public String getVideoTime() {
        return videoTime;
    }

    public void setVideoTime(String videoTime) {
        this.videoTime = videoTime;
    }

    public String getVideoCover() {
        return videoCover;
    }

    public void setVideoCover(String videoCover) {
        this.videoCover = videoCover;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public boolean isSelectedDownload() {
        return isSelectedDownload;
    }

    public void setSelectedDownload(boolean selectedDownload) {
        isSelectedDownload = selectedDownload;
    }

    public boolean isShowSelectButton() {
        return isShowSelectButton;
    }

    public void setShowSelectButton(boolean showSelectButton) {
        isShowSelectButton = showSelectButton;
    }

    @Override
    public String toString() {
        return "HuodeVideoInfo{" +
                "videoTitle='" + videoTitle + '\'' +
                ", videoId='" + videoId + '\'' +
                ", videoTime='" + videoTime + '\'' +
                ", videoCover='" + videoCover + '\'' +
                '}';
    }

    protected HuodeVideoInfo(Parcel in) {
        videoTitle = in.readString();
        videoId = in.readString();
        videoTime = in.readString();
        videoCover = in.readString();
    }

    public static final Creator<HuodeVideoInfo> CREATOR = new Creator<HuodeVideoInfo>() {

        @Override
        public HuodeVideoInfo createFromParcel(Parcel in) {
            return new HuodeVideoInfo(in); // 在构造函数里面完成了 读取 的工作
        }
        //供反序列化本类数组时调用的
        @Override
        public HuodeVideoInfo[] newArray(int size) {
            return new HuodeVideoInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(videoTitle);
        dest.writeString(videoId);
        dest.writeString(videoTime);
        dest.writeString(videoCover);
    }
}
