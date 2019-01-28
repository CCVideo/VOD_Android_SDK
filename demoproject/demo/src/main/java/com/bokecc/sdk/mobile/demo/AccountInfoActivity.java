package com.bokecc.sdk.mobile.demo;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bokecc.sdk.mobile.demo.util.ConfigUtil;
import com.bokecc.sdk.mobile.demo.util.MediaUtil;
import com.bokecc.sdk.mobile.play.MediaMode;

/**
 * 
 * 账户信息界面
 * 
 * @author CC视频
 *
 */
public class AccountInfoActivity extends Activity {

	SharedPreferences sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.account_info_layout);
		super.onCreate(savedInstanceState);

		sp = ((PlayDemoApplication)getApplication()).getAccountSp();

		((TextView)findViewById(R.id.account_api_key)).setText(ConfigUtil.API_KEY);
		((TextView)findViewById(R.id.account_userid)).setText(ConfigUtil.USERID);

		initView();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void initView() {
		initDownloadView();
		initPlayView();
	}

	RadioButton playVideo, playAudio, playAudioVideo;
	private void initPlayView() {
		playVideo = (RadioButton) findViewById(R.id.account_play_video);
		playAudio = (RadioButton) findViewById(R.id.account_play_audio);
		playAudioVideo = (RadioButton) findViewById(R.id.account_play_audiovideo);

		playVideo.setOnClickListener(playOnClickListener);
		playAudio.setOnClickListener(playOnClickListener);
		playAudioVideo.setOnClickListener(playOnClickListener);

		int lastPlayMode = getValue(MediaUtil.SP_PLAY_KEY, MediaUtil.PLAY_MODE.getMode());
		if (lastPlayMode == MediaMode.AUDIO.getMode()) {
			playAudio.toggle();
		} else if (lastPlayMode == MediaMode.VIDEO.getMode()){
			playVideo.toggle();
		} else {
			playAudioVideo.toggle();
		}
	}

	RadioButton downloadVideo, downloadAudio;
	private void initDownloadView() {
		downloadAudio = (RadioButton) findViewById(R.id.account_download_audio);
		downloadVideo = (RadioButton) findViewById(R.id.account_download_video);

		downloadVideo.setOnClickListener(downloadOnClickListener);
		downloadAudio.setOnClickListener(downloadOnClickListener);

		if (getValue(MediaUtil.SP_DOWNLOAD_KEY, MediaUtil.DOWNLOAD_MODE.getMode()) == MediaMode.AUDIO.getMode()) {
			downloadAudio.toggle();
		} else {
			downloadVideo.toggle();
		}
	}

	private View.OnClickListener downloadOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.account_download_video:
					setDownloadMode(MediaMode.VIDEO);
					break;
				case R.id.account_download_audio:
					setDownloadMode(MediaMode.AUDIO);
					break;
			}
		}
	};

	private void setDownloadMode(MediaMode mode) {
		storeValue(MediaUtil.SP_DOWNLOAD_KEY, mode.getMode());
		MediaUtil.DOWNLOAD_MODE = mode;

		switch (mode) {
			case AUDIO:
				MediaUtil.DOWNLOAD_FILE_SUFFIX = MediaUtil.M4A_SUFFIX;
				break;
			default:
				MediaUtil.DOWNLOAD_FILE_SUFFIX = MediaUtil.MP4_SUFFIX;
				break;
		}
	}

	private View.OnClickListener playOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
				case R.id.account_play_audio:
					setPlayMode(MediaMode.AUDIO);
					break;
				case R.id.account_play_video:
					setPlayMode(MediaMode.VIDEO);
					break;
				case R.id.account_play_audiovideo:
					setPlayMode(MediaMode.VIDEOAUDIO);
					break;
			}
		}
	};

	private void setPlayMode(MediaMode mode) {
		storeValue(MediaUtil.SP_PLAY_KEY, mode.getMode());
		MediaUtil.PLAY_MODE = mode;
	}

	private void storeValue(String key, int value) {
		SharedPreferences.Editor editor = sp.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	private int getValue(String key, int defaultValue) {
		return sp.getInt(key, defaultValue);
	}
}