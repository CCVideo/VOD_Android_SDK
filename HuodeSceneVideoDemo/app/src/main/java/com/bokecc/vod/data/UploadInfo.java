package com.bokecc.vod.data;

import com.bokecc.sdk.mobile.upload.VideoInfo;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Transient;

@Entity
public class UploadInfo {

    @Id
    private long id;

    public final static String UPLOAD_PRE = "U_";

    private String uploadId;

    private long start;

    private long end;

    private int status;

    private int progress;

    private String title;

    private String tag;

    private String desc;

    private String filePath;

    private String videoCoverPath;

    private String categoryId;

    private String uploadOrResume;
    private String videoId;
    private String server;
    private String servicetype;
    private String creationTime;
    private String priority;
    private String fileName;
    private String encodetype;
    private String md5;
    private String fileByteSize;
    private boolean isCrop;
    private int expectWidth;
    private int corner = 3;
    private int offsetx = 5;
    private int offsety = 5;
    private int fontfamily = 0;
    private int fontsize = 12;
    private String fontcolor;
    private int fontalpha = 100;
    private String text;

    public UploadInfo() {

    }

    public UploadInfo(String uploadId, long start, long end, int status, int progress) {
        this.uploadId = uploadId;
        this.start = start;
        this.end = end;
        this.status = status;
        this.progress = progress;
    }

    public UploadInfo(String uploadId, long start, long end, int status, int progress, String title, String tag, String desc, String filePath, String categoryId) {
        this.uploadId = uploadId;
        this.start = start;
        this.end = end;
        this.status = status;
        this.progress = progress;
        this.title = title;
        this.tag = tag;
        this.desc = desc;
        this.filePath = filePath;
        this.categoryId = categoryId;
    }

    public String getUploadOrResume() {
        return uploadOrResume;
    }

    public void setUploadOrResume(String uploadOrResume) {
        this.uploadOrResume = uploadOrResume;
    }

    public String getTitle() {
        return title;
    }

    public String getTag() {
        return tag;
    }

    public String getDesc() {
        return desc;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getServicetype() {
        return servicetype;
    }

    public void setServicetype(String servicetype) {
        this.servicetype = servicetype;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getEncodetype() {
        return encodetype;
    }

    public void setEncodetype(String encodetype) {
        this.encodetype = encodetype;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getFileByteSize() {
        return fileByteSize;
    }

    public void setFileByteSize(String fileByteSize) {
        this.fileByteSize = fileByteSize;
    }

    public boolean isCrop() {
        return isCrop;
    }

    public void setCrop(boolean crop) {
        isCrop = crop;
    }

    public int getExpectWidth() {
        return expectWidth;
    }

    public void setExpectWidth(int expectWidth) {
        this.expectWidth = expectWidth;
    }

    public String getVideoCoverPath() {
        return videoCoverPath;
    }

    public void setVideoCoverPath(String videoCoverPath) {
        this.videoCoverPath = videoCoverPath;
    }

    public int getCorner() {
        return corner;
    }

    public void setCorner(int corner) {
        this.corner = corner;
    }

    public int getOffsetx() {
        return offsetx;
    }

    public void setOffsetx(int offsetx) {
        this.offsetx = offsetx;
    }

    public int getOffsety() {
        return offsety;
    }

    public void setOffsety(int offsety) {
        this.offsety = offsety;
    }

    public int getFontfamily() {
        return fontfamily;
    }

    public void setFontfamily(int fontfamily) {
        this.fontfamily = fontfamily;
    }

    public int getFontsize() {
        return fontsize;
    }

    public void setFontsize(int fontsize) {
        this.fontsize = fontsize;
    }

    public String getFontcolor() {
        return fontcolor;
    }

    public void setFontcolor(String fontcolor) {
        this.fontcolor = fontcolor;
    }

    public int getFontalpha() {
        return fontalpha;
    }

    public void setFontalpha(int fontalpha) {
        this.fontalpha = fontalpha;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
