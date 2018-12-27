package com.bokecc.sdk.mobile.demo.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.bokecc.sdk.mobile.demo.downloadutil.DownloadController;
import com.bokecc.sdk.mobile.demo.downloadutil.DownloaderWrapper;
import com.bokecc.sdk.mobile.demo.model.DownloadInfo;
import com.bokecc.sdk.mobile.demo.model.UploadInfo;
import com.bokecc.sdk.mobile.upload.VideoInfo;

public class DataSet {
	private final static String DOWNLOADINFO = "downloadinfo";
	private final static String UPLOADINFO = "uploadinfo";
	private final static String VIDEOPOSITION = "videoposition";
	
	private static Map<String, UploadInfo> uploadInfoMap;
	private static Map<String, DownloadInfo> downloadInfoMap;
	
	private static SQLiteOpenHelper sqLiteOpenHelper;
	
	public static void init(Context context){
		sqLiteOpenHelper = new SQLiteOpenHelper(context, "demo", null, 1) {
			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
			
			@Override
			public void onCreate(SQLiteDatabase db) {
				String downloadSql = "CREATE TABLE IF NOT EXISTS downloadinfo(" +
						"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
						"videoId VERCHAR, " +
						"title VERCHAR, " +
						"start INTEGER, " +
						"end INTEGER, " +
						"status INTEGER, " +
						"createTime DATETIME, " +
						"downloadMode INTEGER, " +
						"definition INTEGER)";
				
				String uploadSql = "CREATE TABLE IF NOT EXISTS uploadinfo(" +
						"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
						"uploadId VERCHAR, status INTEGER, progress INTEGER, progressText VERCHAR, " +
						"videoId VERCHAR, title VERCHAR, tags VERCHAR, description VERCHAR, categoryId VERCHAR, " +
						"filePath VERCHAR, fileName VERCHAR, fileByteSize VERCHAR, md5 VERCHAR, " +
						"uploadServer VERCHAR, serviceType VERCHAR, priority VERCHAR, encodeType VERCHAR, " +
						"uploadOrResume VERCHAR, createTime DATETIME)";
				
				String videoPositionSql = "CREATE TABLE IF NOT EXISTS videoposition(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
						"videoId VERCHAR, " +
						"position INTEGER)";
			
				db.execSQL(downloadSql);
				db.execSQL(uploadSql);
				db.execSQL(videoPositionSql);
				
			}
		};
		
		uploadInfoMap = new HashMap<String, UploadInfo>();
		downloadInfoMap = new HashMap<String, DownloadInfo>();
		reloadData();
	}
	
	public static List<UploadInfo> getUploadInfos(){
		return new ArrayList<UploadInfo>(uploadInfoMap.values());
	}
	
	private static void reloadData(){
		
		SQLiteDatabase db = sqLiteOpenHelper.getReadableDatabase();
		Cursor cursor = null; 
		try {
			// 重载上传信息
			synchronized(uploadInfoMap){
				cursor = db.rawQuery("SELECT * FROM " + UPLOADINFO, null);
				for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
					
					UploadInfo uploadInfo = buildUploadInfo(cursor);
					uploadInfoMap.put(uploadInfo.getUploadId(), uploadInfo);
				}
			}
			
			// 重载下载信息
			synchronized (downloadInfoMap) {
				cursor = db.rawQuery("SELECT * FROM ".concat(DOWNLOADINFO), null);
				for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
					try {
						DownloadInfo downloadInfo = buildDownloadInfo(cursor);
						downloadInfoMap.put(downloadInfo.getTitle(), downloadInfo);
						
					} catch (ParseException e) {
						Log.e("Parse date error", e.getMessage());
					}
				}
			}
		} catch (Exception e) {
			Log.e("cursor error", e.getMessage());
		} finally{
			if (cursor != null) {
				cursor.close();
			}
		}
	}
	
	public static UploadInfo getUploadInfo(String uploadId){
		
		return uploadInfoMap.get(uploadId);
	}
	
	public static void addUploadInfo(UploadInfo uploadInfo){
		synchronized (uploadInfoMap) {
			
			if (uploadInfoMap.containsKey(uploadInfo.getUploadId())) {
				return;
			}
			
			uploadInfoMap.put(uploadInfo.getUploadId(), uploadInfo);
		}
	}
	
	public static void updateUploadInfo(UploadInfo uploadInfo){
		synchronized (uploadInfoMap) {
			uploadInfoMap.put(uploadInfo.getUploadId(), uploadInfo);
		}
	}
	
	public static void removeUploadInfo(String uploadId){
		synchronized(uploadInfoMap){
			uploadInfoMap.remove(uploadId);
		}
	}
	
	public static void saveUploadData(){
		SQLiteDatabase db = sqLiteOpenHelper.getReadableDatabase();
		db.beginTransaction();
		
		try {
			//清除当前数据
			db.delete(UPLOADINFO, null, null);
			for (UploadInfo uploadInfo : uploadInfoMap.values()) {
				
				ContentValues values = new ContentValues();
				values.put("uploadId", uploadInfo.getUploadId());
				values.put("status", uploadInfo.getStatus());
				values.put("progress", uploadInfo.getProgress());
				values.put("progressText", uploadInfo.getProgressText());
				
				VideoInfo videoInfo = uploadInfo.getVideoInfo();
				//video
				values.put("videoId", videoInfo.getVideoId());
				values.put("title", videoInfo.getTitle());
				values.put("tags", videoInfo.getDescription());
				values.put("description", videoInfo.getDescription());
				values.put("filePath", videoInfo.getFilePath());
				values.put("fileName", videoInfo.getFileName());
				values.put("fileByteSize", videoInfo.getFileByteSize());
				values.put("md5", videoInfo.getMd5());
				values.put("uploadServer", videoInfo.getServer());
				values.put("serviceType", videoInfo.getServicetype());
				values.put("priority", videoInfo.getPriority());
				values.put("encodeType", videoInfo.getEncodetype());
				values.put("uploadOrResume", videoInfo.getUploadOrResume());
				values.put("createTime", videoInfo.getCreationTime());
				values.put("categoryId", videoInfo.getCategoryId());
				
				db.insert(UPLOADINFO, null, values);
			}
			
			db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e("db error", e.getMessage());
		} finally {
			db.endTransaction();
		}
		
		db.close();
	}

	public static void saveDownloadData(){
		SQLiteDatabase db = sqLiteOpenHelper.getReadableDatabase();
		db.beginTransaction();

		try {
			//清除当前数据
			db.delete(DOWNLOADINFO, null, null);

			for(DownloaderWrapper wrapper : DownloadController.downloadingList){

				DownloadInfo downloadInfo = wrapper.getDownloadInfo();
				ContentValues values = new ContentValues();
				values.put("videoId", downloadInfo.getVideoId());
				values.put("title", downloadInfo.getTitle());
				values.put("status", downloadInfo.getStatus());
				values.put("definition", downloadInfo.getDefinition());
				values.put("start", downloadInfo.getStart());
				values.put("end", downloadInfo.getEnd());
				values.put("downloadMode", downloadInfo.getDownloadMode());

				SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				values.put("createTime", formater.format(downloadInfo.getCreateTime()));

				db.insert(DOWNLOADINFO, null, values);
			}

			for(DownloaderWrapper wrapper : DownloadController.downloadedList){

				DownloadInfo downloadInfo = wrapper.getDownloadInfo();
				ContentValues values = new ContentValues();
				values.put("videoId", downloadInfo.getVideoId());
				values.put("title", downloadInfo.getTitle());
				values.put("status", downloadInfo.getStatus());
				values.put("definition", downloadInfo.getDefinition());
				values.put("start", downloadInfo.getStart());
				values.put("end", downloadInfo.getEnd());
				values.put("downloadMode", downloadInfo.getDownloadMode());

				SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				values.put("createTime", formater.format(downloadInfo.getCreateTime()));

				db.insert(DOWNLOADINFO, null, values);
			}

			db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e("db error", e.getMessage());
		} finally {
			db.endTransaction();
		}

		db.close();
	}
	
	private static UploadInfo buildUploadInfo(Cursor cursor) {
		String uploadId = cursor.getString(cursor.getColumnIndex("uploadId"));
		int status = cursor.getInt(cursor.getColumnIndex("status"));
		int progress = cursor.getInt(cursor.getColumnIndex("progress"));
		String progressText = cursor.getString(cursor.getColumnIndex("progressText"));
		
		VideoInfo videoInfo = new VideoInfo();
		videoInfo.setVideoId(cursor.getString(cursor.getColumnIndex("videoId")));
		videoInfo.setTitle(cursor.getString(cursor.getColumnIndex("title")));
		videoInfo.setTags(cursor.getString(cursor.getColumnIndex("tags")));
		videoInfo.setDescription(cursor.getString(cursor.getColumnIndex("description")));
		videoInfo.setFilePath(cursor.getString(cursor.getColumnIndex("filePath")));
		videoInfo.setFileName(cursor.getString(cursor.getColumnIndex("fileName")));
		videoInfo.setFileByteSize(cursor.getString(cursor.getColumnIndex("fileByteSize")));
		videoInfo.setMd5(cursor.getString(cursor.getColumnIndex("md5")));
		videoInfo.setServer(cursor.getString(cursor.getColumnIndex("uploadServer")));
		videoInfo.setServicetype(cursor.getString(cursor.getColumnIndex("serviceType")));
		videoInfo.setPriority(cursor.getString(cursor.getColumnIndex("priority")));
		videoInfo.setEncodetype(cursor.getString(cursor.getColumnIndex("encodeType")));
		videoInfo.setUploadOrResume(cursor.getString(cursor.getColumnIndex("uploadOrResume")));
		videoInfo.setCreationTime(cursor.getString(cursor.getColumnIndex("createTime")));
		videoInfo.setCategoryId(cursor.getString(cursor.getColumnIndex("categoryId")));
		
		return new UploadInfo(uploadId, videoInfo, status, progress, progressText);
	}
	
	public static List<DownloadInfo> getDownloadInfos(){
		
		return new ArrayList<DownloadInfo>(downloadInfoMap.values());
	}
	
	public static boolean hasDownloadInfo(String Title){
		return downloadInfoMap.containsKey(Title);
	}
	
	public static DownloadInfo getDownloadInfo(String Title){
		return downloadInfoMap.get(Title);
	}
	
	public static void addDownloadInfo(DownloadInfo downloadInfo){
		synchronized (downloadInfoMap) {
			if (downloadInfoMap.containsKey(downloadInfo.getTitle())) {
				return ;
			}
			
			downloadInfoMap.put(downloadInfo.getTitle(), downloadInfo);

			ContentValues values = new ContentValues();
			values.put("videoId", downloadInfo.getVideoId());
			values.put("title", downloadInfo.getTitle());
			values.put("status", downloadInfo.getStatus());
			values.put("definition", downloadInfo.getDefinition());
			values.put("start", downloadInfo.getStart());
			values.put("end", downloadInfo.getEnd());
			values.put("downloadMode", downloadInfo.getDownloadMode());

			SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			values.put("createTime", formater.format(downloadInfo.getCreateTime()));

			SQLiteDatabase db = sqLiteOpenHelper.getReadableDatabase();
			if (db.isOpen()) {
				db.insert(DOWNLOADINFO, null, values);
				db.close();
			}
		}
	}
	
	public static void removeDownloadInfo(String title){
		synchronized (downloadInfoMap) {
			downloadInfoMap.remove(title);

			SQLiteDatabase database = sqLiteOpenHelper.getWritableDatabase();
			if (database.isOpen()) {
				database.delete(DOWNLOADINFO, "title=?", new String[]{title});
				database.close();
			}
		}
	}
	
	public static void updateDownloadInfo(DownloadInfo downloadInfo){
		synchronized (downloadInfoMap) {
			downloadInfoMap.put(downloadInfo.getTitle(), downloadInfo);
			
			SQLiteDatabase db = sqLiteOpenHelper.getWritableDatabase();
			if (db.isOpen()) {
				ContentValues values = new ContentValues();
				values.put("status", downloadInfo.getStatus());
				values.put("start", downloadInfo.getStart());
				values.put("end", downloadInfo.getEnd());
				db.update(DOWNLOADINFO, values, "title=?", new String[]{downloadInfo.getTitle()});
				db.close();
			}
		}
	}
	
	private static DownloadInfo buildDownloadInfo(Cursor cursor) throws ParseException{
		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date createTime = formater.parse(cursor.getString(cursor.getColumnIndex("createTime")));
		DownloadInfo downloadInfo = new DownloadInfo(cursor.getString(cursor.getColumnIndex("videoId")), 
				cursor.getString(cursor.getColumnIndex("title")),
				cursor.getInt(cursor.getColumnIndex("status")),
				cursor.getLong(cursor.getColumnIndex("start")),
				cursor.getLong(cursor.getColumnIndex("end")),
				createTime,
				cursor.getInt(cursor.getColumnIndex("definition")));
		downloadInfo.setDownloadMode(cursor.getInt(cursor.getColumnIndex("downloadMode")));
		return downloadInfo;
	}

	public static void insertVideoPosition(String videoId, int position) {
		
		SQLiteDatabase database = sqLiteOpenHelper.getWritableDatabase();
		if (database.isOpen()) {
			ContentValues values = new ContentValues();
			values.put("videoId", videoId);
			values.put("position", position);
			database.insert(VIDEOPOSITION, null, values);
			database.close();
		}
	}
	
	public static int getVideoPosition(String videoId) {
		int position = 0;
		SQLiteDatabase database = sqLiteOpenHelper.getReadableDatabase();
		if (database.isOpen()) {
			Cursor cursor = database.query(VIDEOPOSITION, new String[]{"position"}, "videoId=?", new String[]{videoId}, null, null, null);
			if (cursor.moveToFirst()) {
				position = cursor.getInt(cursor.getColumnIndex("position"));
			}
			cursor.close();
			database.close();
		}
		return position;
	}
	
	public static void updateVideoPosition(String videoId, int position) {
		SQLiteDatabase database = sqLiteOpenHelper.getWritableDatabase();
		if (database.isOpen()) {
			ContentValues values = new ContentValues();
			values.put("position", position);
			database.update(VIDEOPOSITION, values, "videoId=?", new String[]{videoId});
			database.close();
		}
	}
}
