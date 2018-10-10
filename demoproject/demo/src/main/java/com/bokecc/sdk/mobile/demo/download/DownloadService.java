package com.bokecc.sdk.mobile.demo.download;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.bokecc.sdk.mobile.demo.downloadutil.DownloadController;
import com.bokecc.sdk.mobile.demo.downloadutil.DownloaderWrapper;

/**
 * DownloadService，用于支持后台下载
 * 
 * @author CC视频
 *
 */
public class DownloadService extends Service {

	Timer timer = new Timer();

	//暂停所有下载任务
	private void pauseAllDownloader() {
		for (DownloaderWrapper wrapper: DownloadController.downloadingList) {
			wrapper.pause();
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				DownloadController.update();
			}
		}, 1 * 1000, 1 * 1000);
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onDestroy() {
		timer.cancel();
		pauseAllDownloader();
		super.onDestroy();
	}

}
