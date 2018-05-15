package com.bokecc.sdk.mobile.demo.play;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.bokecc.sdk.mobile.demo.PlayDemoApplication;
import com.bokecc.sdk.mobile.demo.R;
import com.bokecc.sdk.mobile.demo.play.Subtitle.OnSubtitleInitedListener;
import com.bokecc.sdk.mobile.demo.play.controller.PlayerUtil;
import com.bokecc.sdk.mobile.demo.util.ConfigUtil;
import com.bokecc.sdk.mobile.demo.util.DataSet;
import com.bokecc.sdk.mobile.demo.util.MediaUtil;
import com.bokecc.sdk.mobile.demo.util.ParamsUtil;
import com.bokecc.sdk.mobile.demo.view.HotspotSeekBar;
import com.bokecc.sdk.mobile.demo.view.PlayChangeVideoPopupWindow;
import com.bokecc.sdk.mobile.demo.view.PlayTopPopupWindow;
import com.bokecc.sdk.mobile.demo.view.PopMenu;
import com.bokecc.sdk.mobile.demo.view.PopMenu.OnItemClickListener;
import com.bokecc.sdk.mobile.demo.view.VerticalSeekBar;
import com.bokecc.sdk.mobile.exception.DreamwinException;
import com.bokecc.sdk.mobile.play.DWIjkMediaPlayer;
import com.bokecc.sdk.mobile.play.MediaMode;
import com.bokecc.sdk.mobile.play.OnDreamWinErrorListener;
import com.bokecc.sdk.mobile.play.OnHotspotListener;
import com.bokecc.sdk.mobile.play.OnPlayModeListener;

/**
 * 视频播放界面
 * 
 * @author CC视频
 * 
 */
public class SpeedIjkMediaPlayActivity extends Activity implements
		DWIjkMediaPlayer.OnBufferingUpdateListener,
		DWIjkMediaPlayer.OnInfoListener,
		DWIjkMediaPlayer.OnPreparedListener, DWIjkMediaPlayer.OnErrorListener,
		IMediaPlayer.OnVideoSizeChangedListener, SensorEventListener, IMediaPlayer.OnCompletionListener, TextureView.SurfaceTextureListener, OnDreamWinErrorListener {

	private static final String TAG = "SpeedPlayTag" ;
	private boolean networkConnected = true;
	private DWIjkMediaPlayer player;
	private Subtitle subtitle;
	private TextureView textureView;
	private Surface surface;
	private ProgressBar bufferProgressBar;
	private HotspotSeekBar skbProgress;
	private ImageView backPlayList;
	private TextView videoIdText, playCurrentPosition, videoDuration;
	private TextView tvDefinition;
	private PopMenu definitionMenu, speedMenu;
	private LinearLayout playerTopLayout, volumeLayout;
	private LinearLayout playerBottomLayout;
	private AudioManager audioManager;
	private VerticalSeekBar volumeSeekBar;
	private int currentVolume;
	private int maxVolume;
	private TextView subtitleText;

	private boolean isLocalPlay;
	private boolean isPrepared;
	private Map<String, Integer> definitionMap;

	private Handler playerHandler;
	private Timer timer = new Timer();
	private TimerTask timerTask, networkInfoTimerTask;

	private int currentScreenSizeFlag = 1;
	private int currrentSubtitleSwitchFlag = 0;
	private int currentDefinitionIndex = 0;
	// 默认设置为普清
	private int defaultDefinition = DWIjkMediaPlayer.NORMAL_DEFINITION;
	
	private String path;

	private Boolean isPlaying;
	// 当player未准备好，并且当前activity经过onPause()生命周期时，此值为true
	private boolean isFreeze = false;
	private boolean isSurfaceDestroy = false;

	int currentPosition;
	private Dialog dialog;

	private String[] definitionArray;
	private final String[] screenSizeArray = new String[] { "满屏", "100%", "75%", "50%" };
	private final String[] subtitleSwitchArray = new String[] { "开启", "关闭" };
	private final String subtitleExampleURL = "http://dev.bokecc.com/static/font/example.utf8.srt";

	private GestureDetector detector;
	private float scrollTotalDistance;
	private int lastPlayPosition, currentPlayPosition;
	private String videoId;
	private RelativeLayout rlBelow, rlPlay;
	private WindowManager wm;
	private ImageView ivFullscreen;
	// 隐藏界面的线程
	private Runnable hidePlayRunnable = new Runnable() {
		@Override
		public void run() {
			setLayoutVisibility(View.GONE, false);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 隐藏标题
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		playDemoApplication = (PlayDemoApplication) getApplication();
		setContentView(R.layout.new_ad_media_play);
		
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		
		detector = new GestureDetector(this, new MyGesture());

		initView();

		initPlayHander();

		initPlayInfo();
		
		initSpeedSwitchMenu();
		
		if (!isLocalPlay) {
			initNetworkTimerTask();
		}
	}

	ImageView lockView;
	ImageView ivCenterPlay;
	ImageView ivDownload;
	ImageView ivTopMenu;
	TextView tvChangeVideo, tvSpeedPlay;
	ImageView ivBackVideo, ivNextVideo, ivPlay;

	LinearLayout avChangeLayout;
	TextView changeToVideoPlayView;
	TextView changeToAudioPlayView;
	
	private void initView() {
		
		rlBelow = (RelativeLayout) findViewById(R.id.rl_below_info);
		rlPlay = (RelativeLayout) findViewById(R.id.rl_play);
		rlPlay.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (!isPrepared || isAudio) {
					return true;
				}
				resetHideDelayed();
				
				// 事件监听交给手势类来处理
				detector.onTouchEvent(event);
				return true;
			}
		});
		
		rlPlay.setClickable(true);
		rlPlay.setLongClickable(true);
		rlPlay.setFocusable(true);
		
		ivTopMenu = (ImageView) findViewById(R.id.iv_top_menu);
		ivTopMenu.setOnClickListener(onClickListener);
		
		textureView = (TextureView) findViewById(R.id.playerSurfaceView);
		textureView.setSurfaceTextureListener(this);

		bufferProgressBar = (ProgressBar) findViewById(R.id.bufferProgressBar);
		
		ivCenterPlay = (ImageView) findViewById(R.id.iv_center_play);
		ivCenterPlay.setOnClickListener(onClickListener);

		backPlayList = (ImageView) findViewById(R.id.backPlayList);
		videoIdText = (TextView) findViewById(R.id.videoIdText);
		ivDownload = (ImageView) findViewById(R.id.iv_download_play);
		ivDownload.setOnClickListener(onClickListener);
		
		playCurrentPosition = (TextView) findViewById(R.id.playDuration);
		videoDuration = (TextView) findViewById(R.id.videoDuration);
		playCurrentPosition.setText(ParamsUtil.millsecondsToMinuteSecondStr(0));
		videoDuration.setText(ParamsUtil.millsecondsToMinuteSecondStr(0));
		
		ivBackVideo = (ImageView) findViewById(R.id.iv_video_back);
		ivNextVideo = (ImageView) findViewById(R.id.iv_video_next);
		ivPlay = (ImageView) findViewById(R.id.iv_play);
		
		ivBackVideo.setOnClickListener(onClickListener);
		ivNextVideo.setOnClickListener(onClickListener);
		ivPlay.setOnClickListener(onClickListener);
		
		tvSpeedPlay = (TextView) findViewById(R.id.tv_speed_play);
		tvSpeedPlay.setOnClickListener(onClickListener);
		
		tvChangeVideo = (TextView) findViewById(R.id.tv_change_video);
		tvChangeVideo.setOnClickListener(onClickListener);
		
		tvDefinition = (TextView) findViewById(R.id.tv_definition);

		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

		volumeSeekBar = (VerticalSeekBar) findViewById(R.id.volumeSeekBar);
		volumeSeekBar.setThumbOffset(2);

		volumeSeekBar.setMax(maxVolume);
		volumeSeekBar.setProgress(currentVolume);
		volumeSeekBar.setOnSeekBarChangeListener(volumeSeekBarChangeListener);

		skbProgress = (HotspotSeekBar) findViewById(R.id.skbProgress);
		skbProgress.setOnSeekBarChangeListener(hotspotOnSeekBarChangeListener);
		skbProgress.setOnIndicatorTouchListener(new HotspotSeekBar.OnIndicatorTouchListener() {
			@Override
			public void onIndicatorTouch(int currentPosition) {
				player.seekTo(currentPosition * 1000);
			}
		});

		playerTopLayout = (LinearLayout) findViewById(R.id.playerTopLayout);
		volumeLayout = (LinearLayout) findViewById(R.id.volumeLayout);
		playerBottomLayout = (LinearLayout) findViewById(R.id.playerBottomLayout);
		
		ivFullscreen = (ImageView) findViewById(R.id.iv_fullscreen);
		
		ivFullscreen.setOnClickListener(onClickListener);
		backPlayList.setOnClickListener(onClickListener);
		tvDefinition.setOnClickListener(onClickListener);

		subtitleText = (TextView) findViewById(R.id.subtitleText);
		
		lockView = (ImageView) findViewById(R.id.iv_lock);
		lockView.setSelected(false);
		lockView.setOnClickListener(onClickListener);

		avChangeLayout = (LinearLayout) findViewById(R.id.audio_video_change_layout);
		changeToAudioPlayView = (TextView) findViewById(R.id.change_audio_play);
		changeToAudioPlayView.setOnClickListener(onClickListener);
		changeToVideoPlayView = (TextView) findViewById(R.id.change_video_play);
		changeToVideoPlayView.setOnClickListener(onClickListener);

		initAudioLayout();
	}

	LinearLayout audioLayout;
	TextView audioSpeedView, audioCurrentTimeView, audioDurationView;
	SeekBar audioSeekBar;
	ImageView audioPlayPauseView, audioForward15sView, audioBack15sView;
	private void initAudioLayout() {
		audioLayout = (LinearLayout) findViewById(R.id.audio_layout);
		audioSpeedView = (TextView) findViewById(R.id.audio_speed);
		audioSpeedView.setOnClickListener(onClickListener);

		audioSeekBar = (SeekBar) findViewById(R.id.audioProgress);
		audioSeekBar.setMax(1 * 1000);
		audioSeekBar.setOnSeekBarChangeListener(audioPlayOnSeekBarChangeListener);

		audioCurrentTimeView = (TextView) findViewById(R.id.audio_current_time);
		audioDurationView = (TextView) findViewById(R.id.audio_duration_time);

		audioPlayPauseView = (ImageView) findViewById(R.id.audio_play_pause);
		audioPlayPauseView.setOnClickListener(onClickListener);
		audioForward15sView = (ImageView) findViewById(R.id.audio_forward_15s_view);
		audioForward15sView.setOnClickListener(onClickListener);
		audioBack15sView = (ImageView) findViewById(R.id.audio_back_15s_view);
		audioBack15sView.setOnClickListener(onClickListener);
	}

	private void initPlayHander() {
		playerHandler = new Handler() {
			public void handleMessage(Message msg) {

				if (player == null) {
					return;
				}

				// 刷新字幕
				subtitleText.setText(subtitle.getSubtitleByTime(player
						.getCurrentPosition()));

				// 更新播放进度
				currentPlayPosition = (int)player.getCurrentPosition();
				int duration = (int)player.getDuration();

				if (duration > 0) {
					String currentTime = ParamsUtil.millsecondsToMinuteSecondStr((int)player.getCurrentPosition());
					playCurrentPosition.setText(currentTime);
					audioCurrentTimeView.setText(currentTime);

					long pos = audioSeekBar.getMax() * currentPlayPosition / duration;
					audioSeekBar.setProgress((int) pos);

					skbProgress.setProgress(currentPlayPosition, duration);
				}
			};
		};

	}

	private MediaMode currentPlayMode = null;

	PlayDemoApplication playDemoApplication;

	TreeMap<Integer, String> hotspotMap;

	private void initPlayInfo() {
		
		// 通过定时器和Handler来更新进度
		isPrepared = false;
		player = playDemoApplication.getDWIjkPlayer();

		player.clearMediaData();
		player.reset();

		player.setOnDreamWinErrorListener(this);
		player.setOnErrorListener(this);
		player.setOnCompletionListener(this);
		player.setOnVideoSizeChangedListener(this);
		player.setOnInfoListener(this);
		player.setHttpsPlay(false);

		player.setDefaultPlayMode(MediaUtil.PLAY_MODE, new OnPlayModeListener() {
			@Override
			public void onPlayMode(MediaMode playMode) {
				currentPlayMode = playMode;
			}
		});

		player.setOnHotspotListener(new OnHotspotListener() {
			@Override
			public void onHotspots(TreeMap<Integer, String> hotspotMap) {
				SpeedIjkMediaPlayActivity.this.hotspotMap = hotspotMap;
			}
		});

		switch (MediaUtil.PLAY_MODE) {
			case AUDIO:
				player.setAudioPlay(true);
				break;
			default:
				player.setAudioPlay(false);
				break;
		}
		
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		videoId = getIntent().getStringExtra("videoId");
		videoIdText.setText(videoId);
		isLocalPlay = getIntent().getBooleanExtra("isLocalPlay", false);
		int playModeInteger = getIntent().getIntExtra("playMode", 1);

		String suffix = MediaUtil.MP4_SUFFIX;
		if (playModeInteger == 1) {
			currentPlayMode = MediaMode.VIDEO;
		} else {
			currentPlayMode = MediaMode.AUDIO;
			suffix = MediaUtil.M4A_SUFFIX;
		}

		try {
			if (!isLocalPlay) {// 播放线上视频
				player.setVideoPlayInfo(videoId, ConfigUtil.USERID, ConfigUtil.API_KEY, this);
				player.setDefaultDefinition(defaultDefinition);// 设置默认清晰度
			} else {// 播放本地已下载视频
				player.setVideoPlayInfo(null, null, null, this);
				if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
					path = Environment.getExternalStorageDirectory() + "/".concat(ConfigUtil.DOWNLOAD_DIR).concat("/").concat(videoId).concat(suffix);
				}
			}
		} catch (IllegalArgumentException e) {
			Log.e(TAG, e.getMessage());
		} catch (SecurityException e) {
			Log.e(TAG, e.getMessage());
		} catch (IllegalStateException e) {
			Log.e(TAG, e + "");
		}

		// 设置视频字幕
		subtitle = new Subtitle(new OnSubtitleInitedListener() {
			@Override
			public void onInited(Subtitle subtitle) {
				// 初始化字幕控制菜单
			}
		});
		subtitle.initSubtitleResource(subtitleExampleURL);

	}

	@Override
	public void onPrepared(IMediaPlayer mp) {
		if (this.isDestroyed()) { return; }

		startPlayerTimerTask();
		isPrepared = true;
		
		player.setSpeed(Float.parseFloat(speedArray[currentSpeed]));
		player.setVolume(1.0f, 1.0f);
		
		if (!isFreeze && (isPlaying == null || isPlaying.booleanValue())) {
			startvideoPlay();
		}
		
		if (!isLocalPlay) {
			if (currentPosition > 0) {
				player.seekTo(currentPosition);
			} else {
				lastPlayPosition = DataSet.getVideoPosition(videoId);
				if (lastPlayPosition > 0) {
					player.seekTo(lastPlayPosition);
				}
			}
		}

		definitionMap = player.getDefinitions();
		if (!isLocalPlay) {
			initDefinitionPopMenu();
		}

		bufferProgressBar.setVisibility(View.GONE);
		setSurfaceViewLayout();

		String videoDurationStr = ParamsUtil.millsecondsToMinuteSecondStr((int)player.getDuration());
		videoDuration.setText(videoDurationStr);
		audioDurationView.setText(videoDurationStr);

		avChangeLayout.setVisibility(View.GONE);
		switch (currentPlayMode) {
			case VIDEOAUDIO:
				avChangeLayout.setVisibility(View.VISIBLE);
				break;
			case AUDIO:
				changeAudioPlayLayout();
				break;
			case VIDEO:
				changeVideoPlayLayout();
				break;
		}

		if (hotspotMap != null && hotspotMap.size() > 0) {
			skbProgress.setHotSpotPosition(hotspotMap, player.getDuration() / 1000);
		}

	}
	
	// 设置surfaceview的布局
	private void setSurfaceViewLayout() {
		LayoutParams params = PlayerUtil.getScreenSizeParams(wm, screenSizeArray[currentScreenSizeFlag],
				player.getVideoWidth(), player.getVideoHeight());
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		textureView.setLayoutParams(params);
	}

	private void initDefinitionPopMenu() {
		if(definitionMap.size() > 1){
			currentDefinitionIndex = 1;
			Integer[] definitions = new Integer[]{};
			definitions = definitionMap.values().toArray(definitions);
			// 设置默认为普清，所以此处需要判断一下
			for (int i=0; i<definitions.length; i++) {
				if (definitions[i].intValue() == defaultDefinition) {
					currentDefinitionIndex = i;
				}
			}
		}
		
		definitionMenu = new PopMenu(this, R.drawable.popdown, currentDefinitionIndex, getResources().getDimensionPixelSize(R.dimen.popmenu_height));
		// 设置清晰度列表
		definitionArray = new String[] {};
		definitionArray = definitionMap.keySet().toArray(definitionArray);

		definitionMenu.addItems(definitionArray);
		definitionMenu.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(int position) {
				try {

					currentDefinitionIndex = position;
					defaultDefinition = definitionMap.get(definitionArray[position]);

					if (isPrepared) {
						currentPosition = (int)player.getCurrentPosition();
						if (player.isPlaying()) {
							isPlaying = true;
						} else {
							isPlaying = false;
						}
					}
					
					isPrepared = false;

					setLayoutVisibility(View.GONE, false);
					bufferProgressBar.setVisibility(View.VISIBLE);

					player.reset();
					player.setSurface(surface);
					player.setDefinition(getApplicationContext(), defaultDefinition);

				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				}

			}
		});
	}

	@Override
	public void onBufferingUpdate(IMediaPlayer mp, int percent) {
		if (this.isDestroyed()) {
			return;
		}

		skbProgress.setSecondaryProgress(percent);
		audioSeekBar.setSecondaryProgress(percent);
	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			resetHideDelayed();
			
			switch (v.getId()) {
            case R.id.backPlayList:
                if (PlayerUtil.isPortrait() || isLocalPlay) {
                    finish();
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
                break;
            case R.id.iv_fullscreen:
                if (PlayerUtil.isPortrait()) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
                break;
            case R.id.tv_definition:
                definitionMenu.showAsDropDown(v);
                break;
            case R.id.iv_lock:
                if (lockView.isSelected()) {
                    lockView.setSelected(false);
                    setLayoutVisibility(View.VISIBLE, true);
					PlayerUtil.toastInfo(SpeedIjkMediaPlayActivity.this,"已解开屏幕");
                } else {
                    lockView.setSelected(true);
                    PlayerUtil.setLandScapeRequestOrientation(wm, SpeedIjkMediaPlayActivity.this);
                    setLayoutVisibility(View.GONE, true);
                    lockView.setVisibility(View.VISIBLE);
                    PlayerUtil.toastInfo(SpeedIjkMediaPlayActivity.this, "已锁定屏幕");
                }
                break;
            case R.id.iv_center_play:
            case R.id.iv_play:
                changePlayStatus();
                break;
            case R.id.iv_download_play:
                PlayerUtil.downloadCurrentVideo(SpeedIjkMediaPlayActivity.this, videoId, isAudio);
                break;
            case R.id.iv_top_menu:
                setLayoutVisibility(View.GONE, false);
                showTopPopupWindow();
                break;
            case R.id.tv_change_video:
                setLayoutVisibility(View.GONE, false);
                showChangeVideoWindow();
                break;
            case R.id.iv_video_back:
                changeToBackVideo();
                break;
            case R.id.iv_video_next:
                changeToNextVideo(false);
                break;
            case R.id.tv_speed_play:
                speedMenu.showAsDropDown(v);
                break;
			case R.id.change_audio_play:
				changeAudioPlayLayout();
				prepareAVPlayer(true);
				break;
			case R.id.change_video_play:
				changeVideoPlayLayout();
				prepareAVPlayer(false);
				break;
			case R.id.audio_play_pause:
				changePlayStatus();
				break;
			case R.id.audio_back_15s_view:
				seekToAudioBack15s();
				break;
			case R.id.audio_forward_15s_view:
				seekToAudioForword15s();
				break;
			case R.id.audio_speed:
				changeAudioSpeed();
				break;
			}
		}
	};
	
	// 设定默认播放速度为正常速度
	private int currentSpeed = 1;
	private final String[] speedArray = new String[]{"0.5", "1.0", "1.5", "2.0"};
	private void initSpeedSwitchMenu() {
		tvSpeedPlay.setVisibility(View.VISIBLE);
		speedMenu = new PopMenu(this, R.drawable.popup, currentSpeed, getResources().getDimensionPixelSize(R.dimen.popmenu_speed_height));
		speedMenu.addItems(speedArray);
		speedMenu.setOnItemClickListener(new PopMenu.OnItemClickListener() {

			@Override
			public void onItemClick(int position) {
				PlayerUtil.toastInfo(SpeedIjkMediaPlayActivity.this,speedArray[position]+"倍速度播放");
				if(isPrepared){
					player.setSpeed(Float.parseFloat(speedArray[position]));
					audioSpeedView.setText("语速x" + speedArray[position]);
					currentSpeed = position;
				}
			}
		});
	}

	private void initNetworkTimerTask() {
		networkInfoTimerTask = new TimerTask() {
			@Override
			public void run() {
				parseNetworkInfo();
			}
		};
		timer.schedule(networkInfoTimerTask, 0, 600);
	}

	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {

		this.surface = new Surface(surfaceTexture);

		if (player.isPlaying() && isAudio) {
			return;
		}

		try {
			player.reset();
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			player.setVolume(1.0f, 1.0f);
			player.setOnBufferingUpdateListener(this);
			player.setOnPreparedListener(this);
			player.setSurface(surface);
			player.setScreenOnWhilePlaying(true);

			if (isLocalPlay) {
				player.setDataSource(path);
			}
			player.prepareAsync();
		} catch (Exception e) {
			Log.e(TAG, "error", e);
		}
		Log.i(TAG, "surface created");

	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		if (player == null) {
			return false;
		}
		if (isPrepared) {
			currentPosition = (int)player.getCurrentPosition();
		}

		if (isAudio) {
			return false;
		}

		cancelTimerTask();

		isPrepared = false;
		isSurfaceDestroy = true;

		player.pause();
		player.stop();
		player.reset();
		return false;
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

	@Override
	public void onPlayError(final DreamwinException e) {
		PlayerUtil.toastInfo(this, e.getMessage());
	}

	enum NetworkStatus {
		WIFI,
		MOBILEWEB,
		NETLESS,
	}

	private NetworkStatus currentNetworkStatus;
	ConnectivityManager cm;
	private void parseNetworkInfo() {
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isAvailable()) {
			if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
				if (currentNetworkStatus != null && currentNetworkStatus == NetworkStatus.WIFI) {
					return;
				} else {
					currentNetworkStatus = NetworkStatus.WIFI;
					showWifiToast();
				}

			} else {
				if (currentNetworkStatus != null && currentNetworkStatus == NetworkStatus.MOBILEWEB) {
					return;
				} else {
					currentNetworkStatus = NetworkStatus.MOBILEWEB;
					showMobileDialog();
				}
			}

			startPlayerTimerTask();
			networkConnected = true;
		} else {
			if (currentNetworkStatus != null && currentNetworkStatus == NetworkStatus.NETLESS) {
				return;
			} else {
				currentNetworkStatus = NetworkStatus.NETLESS;
				showNetlessToast();
			}

			cancelTimerTask();

			networkConnected = false;
		}
	}

	private void showWifiToast() {
		PlayerUtil.toastInfo(this, "已切换至wifi");
	}

	private void showMobileDialog() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(SpeedIjkMediaPlayActivity.this);
				AlertDialog dialog = builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						finish();
					}
				}).setPositiveButton("继续", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).setMessage("当前为移动网络，是否继续播放？").create();

				dialog.show();
			}
		});

	}

	private void showNetlessToast() {
		PlayerUtil.toastInfo(SpeedIjkMediaPlayActivity.this, "当前无网络信号，无法播放");
	}

	private void startPlayerTimerTask() {
		cancelTimerTask();

		timerTask = new TimerTask() {
			@Override
			public void run() {

				if (!isPrepared) {
					return;
				}

				playerHandler.sendEmptyMessage(0);
			}
		};

		timer.schedule(timerTask, 0, 1 * 1000);
	}

	private void cancelTimerTask() {
		if (timerTask != null) {
			timerTask.cancel();
		}
	}

	private void changeAudioSpeed() {
		if (!isPrepared) { return; }

		currentSpeed = ++currentSpeed % 4;

		audioSpeedView.setText("语速x" + speedArray[currentSpeed]);
		player.setSpeed(Float.parseFloat(speedArray[currentSpeed]));
		speedMenu.setCheckedPosition(currentSpeed);
	}

	private void prepareAVPlayer(boolean isAudio) {
		if (isPrepared) {
			currentPosition = (int)player.getCurrentPosition();
			if (player.isPlaying()) {
				isPlaying = true;
			} else {
				isPlaying = false;
			}
		}

		isPrepared = false;

		player.reset();
		player.setAudioPlay(isAudio);
		player.setSurface(surface);
		player.prepareAsync();
	}
	
	private void changeToNextVideo(boolean isCompleted) {
		int currentIndex = getCurrentVideoIndex();
		int length = PlayFragment.playVideoIds.length;
		int position = 0;
		if (currentIndex == length - 1) {
			position = 0;
		} else {
			position = ++currentIndex;
		}
		changeVideo(position, isCompleted);
	}
	
	private void changeToBackVideo() {
		int currentPosition = getCurrentVideoIndex();
		int length = PlayFragment.playVideoIds.length;
		int position = 0;
		if (currentPosition == 0) {
			position = length - 1;
		} else {
			position = --currentPosition;
		}
		changeVideo(position, false);
	}
	
	PlayChangeVideoPopupWindow playChangeVideoPopupWindow;
	private void showChangeVideoWindow() {
		if (playChangeVideoPopupWindow == null) {
			initPlayChangeVideoPopupWindow();
		}
		playChangeVideoPopupWindow.setSelectedPosition(getCurrentVideoIndex()).showAsDropDown(rlPlay);
	}
	
	private int getCurrentVideoIndex() {
		return Arrays.asList(PlayFragment.playVideoIds).indexOf(videoId);
	}
	
	private void initPlayChangeVideoPopupWindow() {
		playChangeVideoPopupWindow = new PlayChangeVideoPopupWindow(this, textureView.getHeight());
		
		playChangeVideoPopupWindow.setItem(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				changeVideo(position, false);
				playChangeVideoPopupWindow.setSelectedPosition(position);
				playChangeVideoPopupWindow.refreshView();
			}
		});
	}
	
	private void changeVideo(int position, boolean isCompleted) {
		if (isCompleted) {
			updateCompleteDataPosition();
		} else {
			updateCurrentDataPosition();
		}
		
		isPrepared = false;
		
		setLayoutVisibility(View.GONE, false);

		avChangeLayout.setVisibility(View.GONE);

		bufferProgressBar.setVisibility(View.VISIBLE);
		ivCenterPlay.setVisibility(View.GONE);
		
		currentPosition = 0;
		currentPlayPosition = 0;
		
		cancelTimerTask();
		
		videoId = PlayFragment.playVideoIds[position];
		videoIdText.setText(videoId);

		if (playChangeVideoPopupWindow != null) {
			playChangeVideoPopupWindow.setSelectedPosition(getCurrentVideoIndex()).refreshView();
		}
		
		player.pause();
		player.stop();
		player.reset();
		player.setDefaultDefinition(defaultDefinition);
		player.setVideoPlayInfo(videoId, ConfigUtil.USERID, ConfigUtil.API_KEY, SpeedIjkMediaPlayActivity.this);
		player.setSurface(surface);
		player.setAudioPlay(isAudio);
		player.prepareAsync();
	}
	
	PlayTopPopupWindow playTopPopupWindow;
	private void showTopPopupWindow() {
		if (playTopPopupWindow == null) {
			initPlayTopPopupWindow();
		}
		playTopPopupWindow.showAsDropDown(rlPlay);
	}
	
	private void initPlayTopPopupWindow() {
		playTopPopupWindow = new PlayTopPopupWindow(this, textureView.getHeight());
		playTopPopupWindow.setSubtitleCheckLister(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.rb_subtitle_open:// 开启字幕
					currrentSubtitleSwitchFlag = 0;
					subtitleText.setVisibility(View.VISIBLE);
					break;
				case R.id.rb_subtitle_close:// 关闭字幕
					currrentSubtitleSwitchFlag = 1;
					subtitleText.setVisibility(View.GONE);
					break;
				}
			}
		});
		
		playTopPopupWindow.setScreenSizeCheckLister(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				
				int position = 0;
				switch (checkedId) {
					case R.id.rb_screensize_full:
						position = 0;
						break;
					case R.id.rb_screensize_100:
						position = 1;
						break;
					case R.id.rb_screensize_75:
						position = 2;
						break;
					case R.id.rb_screensize_50:
						position = 3;
						break;
				}
				PlayerUtil.toastInfo(SpeedIjkMediaPlayActivity.this, screenSizeArray[position]);
				currentScreenSizeFlag = position;

				setSurfaceViewLayout();
			}
		});
		
	}

	OnSeekBarChangeListener audioPlayOnSeekBarChangeListener = new OnSeekBarChangeListener() {
		int progress = 0;

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			if (networkConnected || isLocalPlay) {
				player.seekTo(progress);
				playerHandler.postDelayed(hidePlayRunnable, 5 * 1000);
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			playerHandler.removeCallbacks(hidePlayRunnable);
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (networkConnected || isLocalPlay) {
				this.progress = (int)(progress * player.getDuration() / seekBar.getMax());
			}
		}
	};

	HotspotSeekBar.OnSeekBarChangeListener hotspotOnSeekBarChangeListener = new  HotspotSeekBar.OnSeekBarChangeListener() {
		@Override
		public void onStartTrackingTouch(HotspotSeekBar seekBar) {
			playerHandler.removeCallbacks(hidePlayRunnable);
		}

		@Override
		public void onStopTrackingTouch(HotspotSeekBar seekBar, float trackStopPercent) {
			if ((networkConnected || isLocalPlay) && isPrepared) {
				int progress = (int) (trackStopPercent * player.getDuration());
				player.seekTo(progress);
				playerHandler.postDelayed(hidePlayRunnable, 5000);
			}
		}

	};

	VerticalSeekBar.OnSeekBarChangeListener volumeSeekBarChangeListener = new VerticalSeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
			currentVolume = progress;
			volumeSeekBar.setProgress(progress);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			playerHandler.removeCallbacks(hidePlayRunnable);
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			playerHandler.postDelayed(hidePlayRunnable, 5 * 1000);
		}

	};

	// 控制播放器面板显示
	private boolean isDisplay = false;

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// 监测音量变化
		if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN
				|| event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {

			int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			if (currentVolume != volume) {
				currentVolume = volume;
				volumeSeekBar.setProgress(currentVolume);
			}

			if (isPrepared) {
				setLayoutVisibility(View.VISIBLE, true);
			}
		}
		return super.dispatchKeyEvent(event);
	}

	/**
	 * 
	 * @param visibility 显示状态
	 * @param isDisplay 是否延迟消失
	 */
	private void setLayoutVisibility(int visibility, boolean isDisplay) {
		if (player == null || isAudio) {
			return;
		}

		playerHandler.removeCallbacks(hidePlayRunnable);
		
		this.isDisplay = isDisplay;

		if (visibility == View.GONE) {

			if (definitionMenu != null) {
				definitionMenu.dismiss();
			}

			if (skbProgress != null) {
				skbProgress.dismissPopupWindow();
			}

			if (speedMenu != null) {
				speedMenu.dismiss();
			}
		}
		
		if (isDisplay) {
			playerHandler.postDelayed(hidePlayRunnable, 5 * 1000);
		}
		
		if (PlayerUtil.isPortrait()) {
			ivFullscreen.setVisibility(visibility);
			
			lockView.setVisibility(View.GONE);
			
			volumeLayout.setVisibility(View.GONE);
			tvDefinition.setVisibility(View.GONE);
			tvChangeVideo.setVisibility(View.GONE);
			ivTopMenu.setVisibility(View.GONE);
			ivBackVideo.setVisibility(View.GONE);
			ivNextVideo.setVisibility(View.GONE);
			tvSpeedPlay.setVisibility(View.GONE);
		} else {
			ivFullscreen.setVisibility(View.GONE);
			
			lockView.setVisibility(visibility);
			if (lockView.isSelected()) {
				visibility = View.GONE;
			}
			
			volumeLayout.setVisibility(visibility);
			tvDefinition.setVisibility(visibility);
			tvChangeVideo.setVisibility(visibility);
			ivTopMenu.setVisibility(visibility);
			ivBackVideo.setVisibility(visibility);
			ivNextVideo.setVisibility(visibility);
			tvSpeedPlay.setVisibility(visibility);
		}
		
		if (isLocalPlay) {
			ivDownload.setVisibility(View.GONE);
			ivTopMenu.setVisibility(View.GONE);
			
			ivBackVideo.setVisibility(View.GONE);
			ivNextVideo.setVisibility(View.GONE);
			tvChangeVideo.setVisibility(View.GONE);
			tvDefinition.setVisibility(View.GONE);
			ivFullscreen.setVisibility(View.INVISIBLE);
		}
		
		playerTopLayout.setVisibility(visibility);
		playerBottomLayout.setVisibility(visibility);
	}

	private Handler alertHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			PlayerUtil.toastInfo(SpeedIjkMediaPlayActivity.this,"视频异常，无法播放。");
			super.handleMessage(msg);
		}

	};

	@Override
	public boolean onError(IMediaPlayer mp, int what, int extra) {
		if (this.isDestroyed()) {
			return true;
		}

		Message msg = new Message();
		msg.what = what;
		if (alertHandler != null) {
			alertHandler.sendMessage(msg);
		}
		return true;
	}

	@Override
	public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int i, int j) {
		if (this.isDestroyed()) {
			return;
		}
		setSurfaceViewLayout();
	}

	// 重置隐藏界面组件的延迟时间
	private void resetHideDelayed() {
		playerHandler.removeCallbacks(hidePlayRunnable);
		playerHandler.postDelayed(hidePlayRunnable, 5 * 1000);
	}

	// 手势监听器类
	private class MyGesture extends SimpleOnGestureListener {
		
		private Boolean isVideo;
		private float scrollCurrentPosition;
		private float scrollCurrentVolume;

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return super.onSingleTapUp(e);
		}

		@Override
		public void onLongPress(MotionEvent e) {
			super.onLongPress(e);
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			if (lockView.isSelected() || isAudio) {
				return true;
			}
			if (isVideo == null) {
				if (Math.abs(distanceX) > Math.abs(distanceY)) {
					isVideo = true;
				} else {
					isVideo = false;
				}
			}
			
			if (isVideo.booleanValue()) {
				parseVideoScroll(distanceX);
			} else {
				parseSoundScroll(distanceY);
			}

			return super.onScroll(e1, e2, distanceX, distanceY);
		}
		
		private void parseVideoScroll(float distanceX) {
			if (!isDisplay) {
				setLayoutVisibility(View.VISIBLE, true);
			}
			
			scrollTotalDistance += distanceX;

			float duration = (float) player.getDuration();

			float width = wm.getDefaultDisplay().getWidth() * 0.75f; // 设定总长度是多少，此处根据实际调整
			//右滑distanceX为负
			float currentPosition = scrollCurrentPosition - (float) duration * scrollTotalDistance / width;

			if (currentPosition < 0) {
				currentPosition = 0;
			} else if (currentPosition > duration) {
				currentPosition = duration;
			}

			player.seekTo((int) currentPosition);

			playCurrentPosition.setText(ParamsUtil.millsecondsToMinuteSecondStr((int) currentPosition));
			int pos = (int) (audioSeekBar.getMax() * currentPosition / duration);
			audioSeekBar.setProgress(pos);

			skbProgress.setProgress((int)currentPosition, (int)duration);
		}
		
		private void parseSoundScroll(float distanceY) {
			if (!isDisplay) {
				setLayoutVisibility(View.VISIBLE, true);
			}
			scrollTotalDistance += distanceY;
			
			float height = wm.getDefaultDisplay().getHeight() * 0.75f;
			// 上滑distanceY为正
			currentVolume = (int)(scrollCurrentVolume + maxVolume * scrollTotalDistance / height);
			
			if (currentVolume < 0) {
				currentVolume = 0;
			} else if (currentVolume > maxVolume) {
				currentVolume = maxVolume;
			}
			
			volumeSeekBar.setProgress(currentVolume);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			return super.onFling(e1, e2, velocityX, velocityY);
		}

		@Override
		public void onShowPress(MotionEvent e) {
			super.onShowPress(e);
		}

		@Override
		public boolean onDown(MotionEvent e) {
			scrollTotalDistance = 0f;
			isVideo = null;

			scrollCurrentPosition = (float) player.getCurrentPosition();
			scrollCurrentVolume = currentVolume;

			return super.onDown(e);
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if (lockView.isSelected() || isAudio) {
				return true;
			}
			if (!isDisplay) {
				setLayoutVisibility(View.VISIBLE, true);
			}
			changePlayStatus();
			return super.onDoubleTap(e);
		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent e) {
			return super.onDoubleTapEvent(e);
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			if (isDisplay && !skbProgress.isPopupWindowShow()) {
				setLayoutVisibility(View.GONE, false);
			} else {
				setLayoutVisibility(View.VISIBLE, true);
			}
			return super.onSingleTapConfirmed(e);
		}
	}

	private void changePlayStatus() {
		if (!isPrepared) {
			return;
		}

		if (player.isPlaying()) {
			pauseVideoPlay();
			ivCenterPlay.setVisibility(View.VISIBLE);
		} else {
			startvideoPlay();
		}
	}

	private void startvideoPlay() {
		player.start();
		ivPlay.setImageResource(R.drawable.smallstop_ic);
		ivCenterPlay.setVisibility(View.GONE);
		audioPlayPauseView.setImageResource(R.drawable.audio_pause_icon);
	}

	private void pauseVideoPlay() {
		player.pause();
		ivPlay.setImageResource(R.drawable.smallbegin_ic);
		audioPlayPauseView.setImageResource(R.drawable.audio_play_icon);
	}

	@Override
	public boolean onInfo(IMediaPlayer mp, int what, int extra) {
		if (this.isDestroyed()) {
			return false;
		}

		switch(what) {
			case DWIjkMediaPlayer.MEDIA_INFO_BUFFERING_START:
				if (player.isPlaying()) {
					bufferProgressBar.setVisibility(View.VISIBLE);
				}
				break;
			case DWIjkMediaPlayer.MEDIA_INFO_BUFFERING_END:
				bufferProgressBar.setVisibility(View.GONE);
				break;
		}

		return false;
	}

	private int mX, mY, mZ;
    private long lastTimeStamp = 0;  
    private Calendar mCalendar;  
	private SensorManager sensorManager;

	@Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == null) {
            return;
        }

        if (!isAudio && !lockView.isSelected() && (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)) {
            int x = (int) event.values[0];
            int y = (int) event.values[1];
            int z = (int) event.values[2];
            mCalendar = Calendar.getInstance();
            long stamp = mCalendar.getTimeInMillis() / 1000l;

            int px = Math.abs(mX - x);
            int py = Math.abs(mY - y);
            int pz = Math.abs(mZ - z);

            int maxvalue = getMaxValue(px, py, pz);
            if (maxvalue > 2 && (stamp - lastTimeStamp) > 1) {
                lastTimeStamp = stamp;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }
            mX = x;
            mY = y;
            mZ = z;
        }
    }

    /**
     * 获取一个最大值
     *
     * @param px
     * @param py
     * @param pz
     * @return
     */
    private int getMaxValue(int px, int py, int pz) {
        int max = 0;
        if (px > py && px > pz) {
            max = px;
        } else if (py > px && py > pz) {
            max = py;
        } else if (pz > px && pz > py) {
            max = pz;
        }
        return max;
    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	@Override
	public void onBackPressed() {
		if (PlayerUtil.isPortrait() || isLocalPlay) {
			super.onBackPressed();
		} else {
			PlayerUtil.setPortraitRequestOrientation(this);
		}
	}
	
	@Override
	public void onResume() {
		if (isFreeze) {
			isFreeze = false;
			if (isPrepared) {
				player.start();
			}
		} else {
			if (isPlaying != null && isPlaying.booleanValue() && isPrepared) {
				player.start();
			}
		}
		super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
	}

	@Override
	public void onPause() {
		if (!isAudio && isPrepared) {
			// 如果播放器prepare完成，则对播放器进行暂停操作，并记录状态
			if (player.isPlaying()) {
				isPlaying = true;
			} else {
				isPlaying = false;
			}
			player.pause();
		} else {
			// 如果播放器没有prepare完成，则设置isFreeze为true
			isFreeze = true;
		}
		super.onPause();
	}
	
	@Override
	protected void onStop() {
        sensorManager.unregisterListener(this);
        PlayerUtil.setLandScapeRequestOrientation(wm, this);

		if (!isAudio) {
			player.pause();
			player.stop();
			player.reset();
			cancelTimerTask();
		}
		super.onStop();
	}
	
	private void updateCurrentDataPosition() {
		if (!isLocalPlay && currentPlayPosition > 0 ) {
			updateDataPosition();
		}
	}

	private void updateCompleteDataPosition() {
		updateDataPosition();
	}

	private void updateDataPosition() {
		if (DataSet.getVideoPosition(videoId) > 0) {
			DataSet.updateVideoPosition(videoId, currentPlayPosition);
		} else {
			DataSet.insertVideoPosition(videoId, currentPlayPosition);
		}
	}

	@Override
	protected void onDestroy() {
		cancelTimerTask();

		playerHandler.removeCallbacksAndMessages(null);
		playerHandler = null;
		
		alertHandler.removeCallbacksAndMessages(null);
		alertHandler = null;
		
		updateCurrentDataPosition();
		
		if (!isAudio) {
			playDemoApplication.releaseDWIjkMediaPlayer();
	    }
		if (dialog != null) {
			dialog.dismiss();
		}
		if (!isLocalPlay) {
			networkInfoTimerTask.cancel();
		}
		super.onDestroy();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		
		super.onConfigurationChanged(newConfig);
		
		if (isPrepared && currentPlayMode != MediaMode.AUDIO) {
			// 刷新界面
			setLayoutVisibility(View.GONE, false);
			setLayoutVisibility(View.VISIBLE, true);
		}
		
		lockView.setSelected(false);
		
		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			rlBelow.setVisibility(View.VISIBLE);
			ivFullscreen.setImageResource(R.drawable.fullscreen_close);
			
			if (playChangeVideoPopupWindow != null) {
				playChangeVideoPopupWindow.dismiss();
			}
			
			if (playTopPopupWindow != null) {
				playTopPopupWindow.dismiss();
			}
			skbProgress.setHotspotShown(false);
		} else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			rlBelow.setVisibility(View.GONE);
			ivFullscreen.setImageResource(R.drawable.fullscreen_open);
			skbProgress.setHotspotShown(true);
		}
		
		setSurfaceViewLayout();
	}

	@Override
	public void onCompletion(IMediaPlayer mp) {
		if (this.isDestroyed()) {
			return;
		}

		if (isLocalPlay) {
			PlayerUtil.toastInfo(SpeedIjkMediaPlayActivity.this,"播放完成！");
			finish();
			return;
		}

		if (isPrepared) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					PlayerUtil.toastInfo(SpeedIjkMediaPlayActivity.this,"切换中，请稍候……");
					currentPlayPosition = 0;
					currentPosition = 0;
					changeToNextVideo(true);
				}
			});
		}
	}

	//====================audio video change========================
	private void changeVideoPlayLayout() {
		changeAVPlayChoiceViewStyle(changeToVideoPlayView, changeToAudioPlayView);
		audioLayout.setVisibility(View.GONE);
		isAudio = false;
		if (currrentSubtitleSwitchFlag == 0) {
			subtitleText.setVisibility(View.VISIBLE);
		}
	}

	boolean isAudio = false;
	private void changeAudioPlayLayout() {
		changeAVPlayChoiceViewStyle(changeToAudioPlayView, changeToVideoPlayView);
		audioLayout.setVisibility(View.VISIBLE);
		PlayerUtil.setPortraitRequestOrientation(this);
		setLayoutVisibility(View.GONE, false);
		isAudio = true;

		playerTopLayout.setVisibility(View.VISIBLE);
		if(currrentSubtitleSwitchFlag == 0) {
			subtitleText.setVisibility(View.GONE);
		}
	}

	private void changeAVPlayChoiceViewStyle(TextView selectView, TextView deselectView) {
		selectView.setBackgroundResource(R.drawable.av_change_tag_bg);
		selectView.setTextColor(getResources().getColor(R.color.av_change_text_select));
		deselectView.setBackground(null);
		deselectView.setTextColor(getResources().getColor(R.color.av_change_text_normal));
	}

	private void seekToAudioBack15s() {
		if (!isPrepared) {
			return;
		}

		int currentPosition = (int)player.getCurrentPosition();
		int seekToPosition = currentPosition - 15 * 1000;

		if (seekToPosition > 0) {
			player.seekTo(seekToPosition);
		} else {
			player.seekTo(0);
		}
	}

	private void seekToAudioForword15s() {
		if (!isPrepared) {
			return;
		}
		int currentPosition = (int)player.getCurrentPosition();
		int seekToPosition = currentPosition + 15 * 1000;

		if (seekToPosition > player.getDuration()) {
			player.seekTo(player.getDuration());
		} else {
			player.seekTo(seekToPosition);
		}
	}
}