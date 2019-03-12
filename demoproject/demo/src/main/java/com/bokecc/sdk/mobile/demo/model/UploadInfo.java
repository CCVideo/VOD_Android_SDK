package com.bokecc.sdk.mobile.demo.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.bokecc.sdk.mobile.demo.util.MediaUtil;
import com.bokecc.sdk.mobile.demo.util.ParamsUtil;
import com.bokecc.sdk.mobile.upload.Uploader;
import com.bokecc.sdk.mobile.upload.VideoInfo;

public class UploadInfo {
	
	public final static String UPLOAD_PRE = "U_";
	
	private String uploadId;
	
	private VideoInfo videoInfo;
	
	private int status;
	
	private int progress;
	
	private String progressText;
	
	public UploadInfo(String uploadId, VideoInfo videoInfo, int status, int progress, String progressText) {
		super();
		this.uploadId = uploadId;
		this.videoInfo = videoInfo;
		this.status = status;
		this.progress = progress;
		this.progressText = progressText;
	}
	
	public String getUploadId() {
		return uploadId;
	}

	public void setUploadId(String uploadId) {
		this.uploadId = uploadId;
	}

	public VideoInfo getVideoInfo() {
		return videoInfo;
	}

	public void setVideoInfo(VideoInfo videoInfo) {
		this.videoInfo = videoInfo;
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
	
	public String getProgressText() {
		if (progressText == null) {
			String fileSizeStr = ParamsUtil.byteToM(ParamsUtil.getLong(videoInfo.getFileByteSize())).concat("M");
			if (status == Uploader.FINISH) {
				progressText = fileSizeStr.concat(" / ").concat(fileSizeStr);
				
			} else {
				progressText = "0M / ".concat(fileSizeStr);
			}
		}
		
		return progressText;
	}

	public void setProgressText(String progressText) {
		this.progressText = progressText;
	}

	public String getStatusInfo(){
		String statusInfo = "";
		switch (status) {
		case Uploader.WAIT:
			statusInfo = "等待中";
			break;
		case Uploader.UPLOAD:
			statusInfo = "上传中";
			break;
		case Uploader.PAUSE:
			statusInfo = "已暂停";
			break;
		case Uploader.FINISH:
			statusInfo = "已上传";
			break;
		default:
			statusInfo = "上传失败";
			break;
		}
		
		return statusInfo;
	}
	
	public Bitmap getBitmap(Context context){
		Bitmap bitmap = MediaUtil.getVideoFirstFrame(context, Uri.parse(videoInfo.getFilePath()));
		return bitmap;
	}

}
