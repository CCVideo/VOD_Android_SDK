package com.bokecc.sdk.mobile.demo.play.controller;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.bokecc.sdk.mobile.demo.PlayDemoApplication;
import com.bokecc.sdk.mobile.demo.downloadutil.DownloadController;
import com.bokecc.sdk.mobile.demo.util.DataSet;
import com.bokecc.sdk.mobile.demo.util.ParamsUtil;
import com.bokecc.sdk.mobile.play.MediaMode;

public class PlayerUtil {

	// 获得当前屏幕的方向
	public static boolean isPortrait() {
		int mOrientation = PlayDemoApplication.getContext().getResources().getConfiguration().orientation;
		if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			return false;
		} else {
			return true;
		}
	}
	
	// 重新显示广告view的大小
	public static void resizeAdView(Activity activity, WindowManager wm, final ImageView iv, int adWidth, int adHeight) {
		if (adWidth == 0 || adHeight == 0) {
			return;
		}
		int screenWidth = wm.getDefaultDisplay().getWidth();
		int screenHeight = wm.getDefaultDisplay().getHeight();

		if (PlayerUtil.isPortrait()) {
			screenHeight = screenHeight * 2 / 5;
		} else {
			// 全屏下，广告素材为屏幕60%
			screenWidth = screenWidth * 6 / 10;
			screenHeight = screenHeight * 6 / 10;
		}

		// 等比缩放比例计算
		float widthRatio = (float) screenWidth / (float) adWidth;
		float heightRatio = (float) screenHeight / (float) adHeight;

		if (widthRatio > heightRatio) {
			screenWidth = (int) ((float) adWidth * heightRatio);
		} else {
			screenHeight = (int) ((float) adHeight * widthRatio);
		}
		
		final LayoutParams ivAdLayoutParams = new LayoutParams(screenWidth,
				screenHeight);
		ivAdLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				iv.setLayoutParams(ivAdLayoutParams);
			}
		});
	}

	public static void toastInfo(final Activity atc, final String info) {
		atc.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(atc, info, Toast.LENGTH_SHORT).show();
			}
		});
	}

	public static LayoutParams getScreenSizeParams(WindowManager wm, String screenSizeStr, int vWidth, int vHeight) {

		int width = 600;
		int height = 400;
		if (isPortrait()) {
			width = wm.getDefaultDisplay().getWidth();
			height = wm.getDefaultDisplay().getHeight() * 2 / 5; //TODO 根据当前布局更改
		} else {
			width = wm.getDefaultDisplay().getWidth();
			height = wm.getDefaultDisplay().getHeight();
		}

		if (screenSizeStr.indexOf("%") > 0) {// 按比例缩放
			if (vWidth == 0) {
				vWidth = 600;
			}

			if (vHeight == 0) {
				vHeight = 400;
			}

			if (vWidth > width || vHeight > height) {
				float wRatio = (float) vWidth / (float) width;
				float hRatio = (float) vHeight / (float) height;
				float ratio = Math.max(wRatio, hRatio);

				width = (int) Math.ceil((float) vWidth / ratio);
				height = (int) Math.ceil((float) vHeight / ratio);
			} else {
				float wRatio = (float) width / (float) vWidth;
				float hRatio = (float) height / (float) vHeight;
				float ratio = Math.min(wRatio, hRatio);

				width = (int) Math.ceil((float) vWidth * ratio);
				height = (int) Math.ceil((float) vHeight * ratio);
			}

			int screenSize = ParamsUtil.getInt(screenSizeStr.substring(0, screenSizeStr.indexOf("%")));
			width = (width * screenSize) / 100;
			height = (height * screenSize) / 100;
		}

		LayoutParams params = new LayoutParams(width, height);
		return params;
	}

	// 设置横屏的固定方向，禁用掉重力感应方向
	public static void setLandScapeRequestOrientation(WindowManager wm, Activity atc) {
		int rotation = wm.getDefaultDisplay().getRotation();
		// 旋转90°为横屏正向，旋转270°为横屏逆向
		if (rotation == Surface.ROTATION_90) {
			atc.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else if (rotation == Surface.ROTATION_270) {
			atc.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
		}
	}

	// 设置竖屏的固定方向，禁用掉重力感应方向
	public static void setPortraitRequestOrientation(Activity atc) {
		atc.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	public static void downloadCurrentVideo(Activity atc, String videoId,String veriCode, boolean isAudio) {
		String title = videoId;

		MediaMode downloadMode = MediaMode.VIDEO;
		if (isAudio) {
			title = title + "_a";
			downloadMode = MediaMode.AUDIO;
		}

		if (DataSet.hasDownloadInfo(title)) {
			toastInfo(atc, "文件已存在");
			return;
		}

		DownloadController.insertDownloadInfo(videoId,veriCode, title, 0, downloadMode);
		toastInfo(atc, "文件已加入下载队列");
	}
}