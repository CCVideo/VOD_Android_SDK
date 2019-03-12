package com.bokecc.sdk.mobile.demo.upload;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.bokecc.sdk.mobile.demo.R;
import com.bokecc.sdk.mobile.demo.model.UploadInfo;
import com.bokecc.sdk.mobile.demo.util.ConfigUtil;
import com.bokecc.sdk.mobile.demo.util.DataSet;
import com.bokecc.sdk.mobile.demo.util.ParamsUtil;
import com.bokecc.sdk.mobile.exception.DreamwinException;
import com.bokecc.sdk.mobile.upload.UploadListener;
import com.bokecc.sdk.mobile.upload.Uploader;
import com.bokecc.sdk.mobile.upload.VideoInfo;

/**
 * 
 * UploadService，用于支持后台上传功能
 * 
 * @author CC视频
 *
 */
public class UploadService extends Service {

	private final int NOTIFY_ID = 10;

	private Context context;
	
	private int progress;
	
	private String progressText;

	private VideoInfo videoInfo;
	
	private Uploader uploader;
	
	private String uploadId;

	private NotificationManager notificationManager;
	
	private Notification notification;

	private boolean stop = true;
	
	private UploadBinder binder = new UploadBinder();

	public class UploadBinder extends Binder {
		
		public String getUploadId(){
			
			return uploadId;
		}
		
		public int getProgress() {
			return progress;
		}
		
		public String getProgressText(){
			return progressText;
		}

		public void upload() {
			if (uploader == null) {
				return;
			} else if (uploader.getStatus() == Uploader.WAIT) {
				uploader.start();
			} else if (uploader.getStatus() == Uploader.PAUSE) {
				uploader.resume();
			}
		}

		public void pause() {
			if (uploader == null) {
				return;
			}
			uploader.pause();
		}

		public void cancle() {
			if (uploader == null) {
				return;
			}
			
			uploader.cancel();
			stop = true;
		}
		
		public boolean isStop(){
			return stop;
		}
		
		public int getUploaderStatus(){
			if (uploader == null) {
				return Uploader.WAIT;
			}
			
			return uploader.getStatus();
		}

	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		context = getApplicationContext();
		
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("service", "start service");
		
		if (intent == null) {
			Log.i("upload service", "intent is null");
			return Service.START_STICKY;
		}
		
		if (uploader != null) {
			Log.i("upload service", "uploader is working.");
			return Service.START_STICKY;
		}
		
		// 获取当前上传ID
		uploadId = intent.getStringExtra("uploadId");
		
		// 根据是否存在videoId，判断是否为首次上传
		String videoId = intent.getStringExtra("videoId");
		if (videoId == null) {// 首次上传
			videoInfo = new VideoInfo();
			videoInfo.setTitle(intent.getStringExtra("title"));
			videoInfo.setTags(intent.getStringExtra("tag"));
			videoInfo.setDescription(intent.getStringExtra("desc"));
			videoInfo.setFilePath(intent.getStringExtra("filePath"));
			videoInfo.setCategoryId(intent.getStringExtra("categoryId"));
		} else {// 续传
			
			videoInfo = DataSet.getUploadInfo(uploadId).getVideoInfo();
		}
		
		if (videoInfo == null) {
			return Service.START_STICKY;
		}
		
		resetUploadService();
		
		// 设置通知栏
		setUpNotification();
		
		// 通知Upload receiver
		Intent broadCastIntent = new Intent(ConfigUtil.ACTION_UPLOAD);
		broadCastIntent.putExtra("uploadId", uploadId);
		DataSet.updateUploadInfo(new UploadInfo(uploadId, videoInfo, Uploader.WAIT, progress, progressText));
		sendBroadcast(broadCastIntent);
		stop = false;
		
		videoInfo.setUserId(ConfigUtil.USERID);
		videoInfo.setNotifyUrl(ConfigUtil.NOTIFY_URL);

		uploader = new Uploader(videoInfo, ConfigUtil.API_KEY);
		uploader.setUploadListener(uploadListenner);
		uploader.start();

		Log.i("init " + videoInfo.getTitle(), "uploadId: " + uploadId);
		return super.onStartCommand(intent, flags, startId);
	}

	
	@Override
	public void onTaskRemoved(Intent rootIntent) {
		
		Log.i("Upload service", "task removed.");
		
		if (uploader != null) {
			uploader.cancel();
			resetUploadService();
		}

		notificationManager.cancel(NOTIFY_ID);
		super.onTaskRemoved(rootIntent);
	}

	UploadListener uploadListenner = new UploadListener() {

		@SuppressLint("NewApi")
		@SuppressWarnings("deprecation")
		@Override
		public void handleStatus(VideoInfo v, int status) {
			videoInfo = v;
			Intent intent = new Intent(ConfigUtil.ACTION_UPLOAD);
			intent.putExtra("uploadId", uploadId);
			intent.putExtra("status", status);
			
			updateUploadInfoByStatus(status);
			
			switch (status) {
			case Uploader.PAUSE:
				sendBroadcast(intent);
				Log.i("upload service", "pause.");
				break;
			case Uploader.UPLOAD:
				sendBroadcast(intent);
				Log.i("upload service", "upload.");
				break;
			case Uploader.FINISH:
				// 下载完毕后变换通知形式
				notification.flags = Notification.FLAG_AUTO_CANCEL;
				notification.contentView.setTextViewText(R.id.progressRate, "上传完成");
				// 停掉服务自身
				stopSelf();
				
				resetUploadService();
				// 通知更新
				notificationManager.notify(NOTIFY_ID, notification);
				// 通知上传队列更新
				sendBroadcast(intent);
				
				Log.i("upload service", "finish.");
				break;
			}

		}

		@Override
		public void handleProcess(long range, long size, String videoId) {
			if (stop) {
				return;
			}
			
			int p = (int) ((double) range / size * 100);
			if (progress <= 100) {
				if (p == progress) {
					return;
				}
				progressText = ParamsUtil.byteToM(range).
						concat("M / ").
						concat(ParamsUtil.byteToM(ParamsUtil.getLong(videoInfo.getFileByteSize()))).
						concat("M");
				progress = p;
				if (progress % 10 == 0) {
					
					progress = p;
					// 更新进度
					RemoteViews contentView = notification.contentView;
					contentView.setTextViewText(R.id.progressRate, progress + "%");
					contentView.setProgressBar(R.id.progress, 100, progress, false);
					// 通知更新
					notificationManager.notify(NOTIFY_ID, notification);
				}

			}
		}

		@Override
		public void handleException(DreamwinException exception, int status) {
			updateUploadInfoByStatus(status);
			// 停掉服务自身
			stopSelf();
			
			Intent intent = new Intent(ConfigUtil.ACTION_UPLOAD);
			intent.putExtra("errorCode", exception.getErrorCode().Value());
			sendBroadcast(intent);
			
			Log.e("上传失败", exception.getMessage());
			
			notificationManager.cancel(NOTIFY_ID);
		}

		@Override
		public void handleCancel(String videoId) {
			stopSelf();
			resetUploadService();
			notificationManager.cancel(NOTIFY_ID);
		}

		@Override
		public void onVideoInfoUpdate(VideoInfo videoInfo) {

		}
	};

	@SuppressWarnings("deprecation")
	private void setUpNotification() {
		
		RemoteViews contentView = new RemoteViews(this.getPackageName(), R.layout.notification_layout);
		contentView.setTextViewText(R.id.fileName, videoInfo.getTitle());
		
		Notification.Builder builder = new Notification.Builder(context)
				.setContentTitle("开始上传").setContent(contentView)
				.setSmallIcon(R.drawable.ic_launcher)
				.setWhen(System.currentTimeMillis())// 设置时间发生时间
				.setAutoCancel(true);// 设置可以清除;
		
		notification = builder.getNotification();

		// 放置在"正在运行"栏目中
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		notificationManager.notify(NOTIFY_ID, notification);
	}

	private void resetUploadService(){
		progress = 0;
		progressText = null;
		uploader = null;
		stop = true;
	}
	
	private void updateUploadInfoByStatus(int status){
		UploadInfo uploadInfo = DataSet.getUploadInfo(uploadId);
		if (uploadInfo == null) {
			return;
		}
		
		uploadInfo.setStatus(status);
		uploadInfo.setVideoInfo(videoInfo);
		
		if (progress > 0) {
			uploadInfo.setProgress(progress);
		}
		
		if (progressText != null) {
			uploadInfo.setProgressText(progressText);
		}
		
		DataSet.updateUploadInfo(uploadInfo);
	}
}
