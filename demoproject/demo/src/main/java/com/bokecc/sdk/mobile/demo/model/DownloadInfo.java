package com.bokecc.sdk.mobile.demo.model;

import java.util.Date;

public class DownloadInfo {
	
	private int id;
	
	private String videoId;
	
	private String title;
	
	private int status;
	
	private Date createTime;
	
	private int definition;

	private long start;

	private long end;

	//设置下载模式
	private int downloadMode = 1;

	public DownloadInfo(String videoId, String title, int status, long start, long end, Date createTime) {
		this.videoId = videoId;
		this.title = title;
		this.status = status;
		this.createTime = createTime;
		this.definition = -1;
		this.start = start;
		this.end = end;
	}
	
	public DownloadInfo(String videoId, String title, int status, long start, long end, Date createTime, int definition) {
		this(videoId, title, status, start, end, createTime);
		this.definition = definition;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getVideoId() {
		return videoId;
	}

	public void setVideoId(String videoId) {
		this.videoId = videoId;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	public int getDefinition() {
		return definition;
	}

	public void setDefinition(int definition) {
		this.definition = definition;
	}
	
	public long getStart() {
		return start;
	}

	public DownloadInfo setStart(long start) {
		this.start = start;
		return this;
	}

	public long getEnd() {
		return end;
	}

	public DownloadInfo setEnd(long end) {
		this.end = end;
		return this;
	}

	public int getDownloadMode() {
		return downloadMode;
	}

	public void setDownloadMode(int downloadMode) {
		this.downloadMode = downloadMode;
	}

}
