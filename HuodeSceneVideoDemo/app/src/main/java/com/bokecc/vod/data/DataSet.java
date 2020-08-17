package com.bokecc.vod.data;

import android.util.Log;

import java.util.List;

import io.objectbox.BoxStore;

public class DataSet {
	private static DownloadDBHelper downloadDBHelper;
	private static UploadDBHelper uploadDBHelper;
	public static void init(BoxStore boxStore){
		downloadDBHelper = new DownloadDBHelper(boxStore);
		uploadDBHelper = new UploadDBHelper(boxStore);
	}

	public static void saveDownloadData(){
		downloadDBHelper.saveDownloadData();
	}

	public static void saveUploadData(){
		uploadDBHelper.saveUploadData();
	}

	public static List<DownloadInfo> getDownloadInfos(){
		return downloadDBHelper.getDownloadInfos();
	}

	public static boolean hasDownloadInfo(String title){
		return downloadDBHelper.hasDownloadInfo(title);
	}

	public static DownloadInfo getDownloadInfo(String title){
		return downloadDBHelper.getDownloadInfo(title);
	}

	public static void addDownloadInfo(DownloadInfo downloadInfo){
		downloadDBHelper.addDownloadInfo(downloadInfo);
	}

	public static void removeDownloadInfo(DownloadInfo downloadInfo){
		downloadDBHelper.removeDownloadInfo(downloadInfo);
	}

	public static void updateDownloadInfo(DownloadInfo downloadInfo){
		downloadDBHelper.updateDownloadInfo(downloadInfo);
	}

	public static void updateUploadInfo(UploadInfo newUploadInfo){
		uploadDBHelper.updateUploadInfo(newUploadInfo);
	}

	public static List<UploadInfo> getUploadInfos(){
		return uploadDBHelper.getUploadInfos();
	}

	public static void addUploadInfo(UploadInfo newUploadInfo){
		uploadDBHelper.addUploadInfo(newUploadInfo);
	}

	public static void removeUploadInfo(UploadInfo newUploadInfo){
		uploadDBHelper.removeUploadInfo(newUploadInfo);
	}
}
