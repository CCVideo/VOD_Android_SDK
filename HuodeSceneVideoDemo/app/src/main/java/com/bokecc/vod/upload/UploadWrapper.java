package com.bokecc.vod.upload;

import android.content.Context;
import android.text.format.Formatter;
import android.util.Log;

import com.bokecc.sdk.mobile.exception.HuodeException;
import com.bokecc.sdk.mobile.upload.UploadListener;
import com.bokecc.sdk.mobile.upload.Uploader;
import com.bokecc.sdk.mobile.upload.VideoInfo;
import com.bokecc.vod.ConfigUtil;
import com.bokecc.vod.data.DataSet;
import com.bokecc.vod.data.UploadInfo;
import com.bokecc.vod.utils.MultiUtils;

/**
 * 上传Uploader包装类
 */

public class UploadWrapper {
    Uploader uploader;
    UploadInfo mUploadInfo;

    long lastStart;

    public UploadWrapper(final UploadInfo uploadInfo) {
        this.mUploadInfo = uploadInfo;

        lastStart = uploadInfo.getStart();
        VideoInfo videoInfo = new VideoInfo();
        videoInfo.setTitle(uploadInfo.getTitle());
        videoInfo.setTags(uploadInfo.getTag());
        videoInfo.setDescription(uploadInfo.getDesc());
        videoInfo.setCategoryId(uploadInfo.getCategoryId());
        videoInfo.setFilePath(uploadInfo.getFilePath());
        videoInfo.setVideoId(uploadInfo.getVideoId());
        videoInfo.setUserId(ConfigUtil.USERID);
        videoInfo.setServer(uploadInfo.getServer());
        videoInfo.setServicetype(uploadInfo.getServicetype());
        videoInfo.setCreationTime(uploadInfo.getCreationTime());
        videoInfo.setPriority(uploadInfo.getPriority());
        videoInfo.setFileName(uploadInfo.getFileName());
        videoInfo.setEncodetype(uploadInfo.getEncodetype());
        videoInfo.setUploadOrResume(uploadInfo.getUploadOrResume());
        videoInfo.setMd5(uploadInfo.getMd5());
        videoInfo.setFileByteSize(uploadInfo.getFileByteSize());
        videoInfo.setCrop(uploadInfo.isCrop());
        videoInfo.setExpectWidth(uploadInfo.getExpectWidth());
        videoInfo.setCorner(uploadInfo.getCorner());
        videoInfo.setOffsetx(uploadInfo.getOffsetx());
        videoInfo.setOffsety(uploadInfo.getOffsety());
        videoInfo.setFontfamily(uploadInfo.getFontfamily());
        videoInfo.setFontsize(uploadInfo.getFontsize());
        videoInfo.setFontcolor(uploadInfo.getFontcolor());
        videoInfo.setFontalpha(uploadInfo.getFontalpha());
        videoInfo.setText(uploadInfo.getText());

        uploader = new Uploader(videoInfo, ConfigUtil.API_KEY);
        uploader.setUploadListener(new UploadListener() {
            @Override
            public void handleProcess(long range, long size, String videoId) {
                mUploadInfo.setStart(range);
                mUploadInfo.setEnd(size);
            }

            @Override
            public void handleException(HuodeException exception, int status) {
                mUploadInfo.setStatus(status);
            }

            @Override
            public void handleStatus(VideoInfo videoInfo, int status) {
                if (status == mUploadInfo.getStatus()) {
                    return;
                } else {
                    mUploadInfo.setStatus(status);
                    DataSet.updateUploadInfo(mUploadInfo);
                }
            }

            @Override
            public void handleCancel(String s) {

            }

            @Override
            public void onVideoInfoUpdate(VideoInfo videoInfo) {
                String uploadOrResume = videoInfo.getUploadOrResume();
                if (uploadOrResume.equals(VideoInfo.RESUME_UPLOAD)) {
                    mUploadInfo.setVideoId(videoInfo.getVideoId());
                    mUploadInfo.setServer(videoInfo.getServer());
                    mUploadInfo.setServicetype(videoInfo.getServicetype());
                    mUploadInfo.setCreationTime(videoInfo.getCreationTime());
                    mUploadInfo.setPriority(videoInfo.getPriority());
                    mUploadInfo.setFileName(videoInfo.getFileName());
                    mUploadInfo.setEncodetype(videoInfo.getEncodetype());
                    mUploadInfo.setUploadOrResume(videoInfo.getUploadOrResume());
                    mUploadInfo.setMd5(videoInfo.getMd5());
                    mUploadInfo.setFileByteSize(videoInfo.getFileByteSize());
                    mUploadInfo.setCrop(videoInfo.isCrop());
                    mUploadInfo.setExpectWidth(videoInfo.getExpectWidth());

                    DataSet.updateUploadInfo(mUploadInfo);
                }

            }
        });


        if (mUploadInfo.getStatus() == Uploader.UPLOAD) {
            start();
        }

    }

    public UploadInfo getUploadInfo() {
        return mUploadInfo;
    }

    public int getStatus() {
        return mUploadInfo.getStatus();
    }

    public String getDownloadProgressText(Context context) {
        String start = Formatter.formatFileSize(context, mUploadInfo.getStart());
        String end = Formatter.formatFileSize(context, mUploadInfo.getEnd());
        String downloadText = String.format("%s/%s", start, end);
        return downloadText;
    }

    public long getDownloadProgressBarValue() {
        if (mUploadInfo.getEnd() == 0) {
            return 0;
        } else {
            return mUploadInfo.getStart() * 100 / mUploadInfo.getEnd();
        }
    }

    public String getSpeed(Context context) {
        String speed = Formatter.formatFileSize(context, mUploadInfo.getStart() - lastStart) + "/s";
        lastStart = mUploadInfo.getStart();
        return speed;
    }

    public void start() {
        mUploadInfo.setStatus(Uploader.UPLOAD);
        uploader.start();
    }


    public void setToWait() {
        mUploadInfo.setStatus(Uploader.WAIT);
        uploader.pause();
    }

    public void pause() {
        mUploadInfo.setStatus(Uploader.PAUSE);
        uploader.pause();
    }

    public void cancel() {
        mUploadInfo.setStatus(Uploader.PAUSE);
        uploader.cancel();
    }
}
