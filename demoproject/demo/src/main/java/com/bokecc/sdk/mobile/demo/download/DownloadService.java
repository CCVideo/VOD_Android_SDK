package com.bokecc.sdk.mobile.demo.download;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

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
	private NetChangedReceiver netReceiver;

	//暂停所有下载任务
	private void pauseAllDownloader() {
		for (DownloaderWrapper wrapper: DownloadController.downloadingList) {
			wrapper.pause();
		}
	}
	

	@Override
	public void onCreate() {
		super.onCreate();
        if (netReceiver == null) {
            netReceiver = new NetChangedReceiver();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netReceiver, filter);

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
		if (netReceiver !=null){
            unregisterReceiver(netReceiver);
        }
		super.onDestroy();
	}

    class NetChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                NetworkInfo dataInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (wifiInfo.isConnected() && dataInfo.isConnected()) {
                    //wifi和移动数据同时连接
                    DownloadController.resumeDownLoad();
                } else if (wifiInfo.isConnected() && !dataInfo.isConnected()) {
                    //wifi已连接，移动数据断开，恢复下载
                    DownloadController.resumeDownLoad();
                } else if (!wifiInfo.isConnected() && dataInfo.isConnected()) {
                    //wifi断开 移动数据连接
                } else {
                    //wifi断开 移动数据断开
                }
            } else {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                Network[] networks = connectivityManager.getAllNetworks();
                int nets = 0;
                for (int i = 0; i < networks.length; i++) {
                    NetworkInfo netInfo = connectivityManager.getNetworkInfo(networks[i]);
                    if (netInfo.getType() == ConnectivityManager.TYPE_MOBILE && !netInfo.isConnected()) {
                        nets += 1;
                    }

                    if (netInfo.getType() == ConnectivityManager.TYPE_MOBILE && netInfo.isConnected()) {
                        nets += 2;
                    }

                    if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        nets += 4;
                    }
                }

                switch (nets) {
                    case 0:
                        //wifi断开 移动数据断开
                        break;
                    case 2:
                        //wifi断开 移动数据连接
                        break;
                    case 4:
                        //wifi已连接，移动数据断开，恢复下载
                        DownloadController.resumeDownLoad();
                        break;
                    case 5:
                        //wifi和移动数据同时连接
                        DownloadController.resumeDownLoad();
                        break;
                }
            }
        }
    }

}
