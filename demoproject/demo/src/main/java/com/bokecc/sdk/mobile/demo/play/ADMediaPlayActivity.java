package com.bokecc.sdk.mobile.demo.play;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
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
import android.view.ViewGroup;
import android.view.ViewParent;
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
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.sdk.mobile.ad.DWMediaAD;
import com.bokecc.sdk.mobile.ad.DWMediaADListener;
import com.bokecc.sdk.mobile.ad.FrontADInfo;
import com.bokecc.sdk.mobile.ad.FrontADInfo.AdBean;
import com.bokecc.sdk.mobile.ad.PauseADInfo;
import com.bokecc.sdk.mobile.demo.R;
import com.bokecc.sdk.mobile.demo.downloadutil.DownloadController;
import com.bokecc.sdk.mobile.demo.play.Subtitle.OnSubtitleInitedListener;
import com.bokecc.sdk.mobile.demo.play.controller.PlayerUtil;
import com.bokecc.sdk.mobile.demo.util.ConfigUtil;
import com.bokecc.sdk.mobile.demo.util.DataSet;
import com.bokecc.sdk.mobile.demo.util.ParamsUtil;
import com.bokecc.sdk.mobile.demo.view.HotspotSeekBar;
import com.bokecc.sdk.mobile.demo.view.PlayChangeVideoPopupWindow;
import com.bokecc.sdk.mobile.demo.view.PlayTopPopupWindow;
import com.bokecc.sdk.mobile.demo.view.PopMenu;
import com.bokecc.sdk.mobile.demo.view.PopMenu.OnItemClickListener;
import com.bokecc.sdk.mobile.demo.view.VerticalSeekBar;
import com.bokecc.sdk.mobile.exception.DreamwinException;
import com.bokecc.sdk.mobile.play.DWMediaPlayer;
import com.bokecc.sdk.mobile.play.OnDreamWinErrorListener;
import com.bokecc.sdk.mobile.play.OnSubtitleMsgListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 视频播放界面
 *
 * @author CC视频
 */
public class ADMediaPlayActivity extends Activity implements
        DWMediaPlayer.OnBufferingUpdateListener,
        DWMediaPlayer.OnInfoListener,
        OnPreparedListener, DWMediaPlayer.OnErrorListener,
        MediaPlayer.OnVideoSizeChangedListener, SensorEventListener, OnCompletionListener, TextureView.SurfaceTextureListener, OnDreamWinErrorListener {

    private boolean networkConnected = true;
    private DWMediaPlayer player;
    private Subtitle subtitle;
    private TextureView textureView;
    private Surface surface;
    private ProgressBar bufferProgressBar;
    private HotspotSeekBar skbProgress;
    private ImageView backPlayList;
    private TextView videoIdText, playCurrentPosition, videoDuration;
    private TextView tvDefinition;
    private PopMenu definitionMenu;
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
    private int defaultDefinition = DWMediaPlayer.NORMAL_DEFINITION;

    private boolean firstInitDefinition = true;
    private String path;

    private Boolean isPlaying;
    // 当player未准备好，并且当前activity经过onPause()生命周期时，此值为true
    private boolean isFreeze = false;
    private boolean isSurfaceDestroy = false;

    //是否点击了暂停广告
    private boolean isClickPauseAd = false;

    int currentPosition;
    private Dialog dialog;

    private String[] definitionArray;
    private final String[] screenSizeArray = new String[]{"满屏", "100%", "75%", "50%"};
    private final String[] subtitleSwitchArray = new String[]{"开启", "关闭"};
    private final String subtitleExampleURL = "http://dev.bokecc.com/static/font/example.utf8.srt";

    private GestureDetector detector;
    private float scrollTotalDistance;
    private int lastPlayPosition, currentPlayPosition;
    private String videoId, verificationCode;
    private RelativeLayout rlBelow, rlPlay;
    private WindowManager wm;
    private ImageView ivFullscreen;

    //Bug 切换视频广告不播放
    private Surface adSurface;
    private SurfaceTexture adSurfaceTexture;
    private boolean isSwitchVideo = false;
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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.new_ad_media_play);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        detector = new GestureDetector(this, new MyGesture());

        videoId = getIntent().getStringExtra("videoId");
        verificationCode = getIntent().getStringExtra("verifyCode");
        isLocalPlay = getIntent().getBooleanExtra("isLocalPlay", false);

        getAdInfo();

        initView();

        initPlayHander();

        initPlayInfo();

        if (!isLocalPlay) {
            initNetworkTimerTask();
        }
    }

    private void initNetworkTimerTask() {
        networkInfoTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (isPrepared) {
                    parseNetworkInfo();
                }
            }
        };

        timer.schedule(networkInfoTimerTask, 0, 600);
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        try {
            adSurfaceTexture = surfaceTexture;
            this.surface = new Surface(surfaceTexture);
            player.reset();
            player.setSurface(surface);
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setOnBufferingUpdateListener(this);
            player.setOnPreparedListener(this);
            player.setScreenOnWhilePlaying(true);
            if (isSurfaceDestroy) {
                if (isLocalPlay) {
                    player.setDataSource(path);
                }
            }

            if (adCount < 0 && isSurfaceDestroy) {
                player.prepareAsync();
            } else {
                if (isVideoAd && isSurfaceDestroy) {
                    initAdPlayer(surface);
                }
            }
            isSurfaceDestroy = false;
        } catch (Exception e) {
            Log.e("videoPlayer", "error", e);
        }
        Log.i("videoPlayer", "surface created");

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        isPrepared = false;
        isSurfaceDestroy = true;

        if (adCount > -1) {
            if (adPlayer != null) {
                try {
                    currentAdPosition = adPlayer.getCurrentPosition();
                } catch (Exception e) {
                }
                for (MediaPlayer player : adPlayers) {
                    player.reset();
                    player.release();
                }
                adPlayers.clear();
            }
            return false;
        }

        if (player == null) {
            return false;
        }

        setLayoutVisibility(View.GONE, false);
        hidePauseAd(); //播放器重置，所以隐藏暂停广告以防其他的问题。

        if (isPrepared) {
            try {
                currentPosition = player.getCurrentPosition();

            } catch (Exception e) {
            }
        }

        if (timerTask != null) {
            timerTask.cancel();
        }

        player.stop();
        player.reset();

        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onPlayError(final DreamwinException e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

            if (timerTask != null) {
                timerTask.cancel();
            }

            networkConnected = false;
        }
    }

    private void showWifiToast() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "已切换至wifi", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showMobileDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(ADMediaPlayActivity.this);
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
                        player.start();
                    }
                }).setMessage("当前为移动网络，是否继续播放？").create();
                player.pause();
                dialog.setCancelable(false);
                dialog.show();
            }
        });

    }

    private void showNetlessToast() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "当前无网络信号，无法播放", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startPlayerTimerTask() {
        if (timerTask != null) {
            timerTask.cancel();
        }
        timerTask = new TimerTask() {
            @Override
            public void run() {

                if (!isPrepared) {
                    return;
                }

                playerHandler.sendEmptyMessage(0);
            }
        };
        timer.schedule(timerTask, 0, 1000);
    }

    ImageView lockView;
    ImageView ivCenterPlay;
    ImageView ivDownload;
    ImageView ivTopMenu;
    TextView tvChangeVideo;
    ImageView ivBackVideo, ivNextVideo, ivPlay;

    private void initView() {

        rlBelow = (RelativeLayout) findViewById(R.id.rl_below_info);
        rlPlay = (RelativeLayout) findViewById(R.id.rl_play);

        rlPlay.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isPrepared) {
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

    }

    private void initPlayHander() {
        playerHandler = new Handler() {
            public void handleMessage(Message msg) {

                if (player == null) {
                    return;
                }

                // 刷新字幕
                if (subtitle != null) {
                    subtitleText.setText(subtitle.getSubtitleByTime(player
                            .getCurrentPosition()));
                }

                // 更新播放进度
                currentPlayPosition = player.getCurrentPosition();
                int duration = player.getDuration();

                if (duration > 0) {
                    String currentTime = ParamsUtil.millsecondsToMinuteSecondStr(player.getCurrentPosition());
                    playCurrentPosition.setText(currentTime);

                    skbProgress.setProgress(currentPlayPosition, duration);
                }
            }

            ;
        };


    }

    private void initPlayInfo() {

        // 通过定时器和Handler来更新进度
        isPrepared = false;
        player = new DWMediaPlayer();
        player.reset();

        player.setOnDreamWinErrorListener(this);
        player.setHttpsPlay(false);
        player.setOnErrorListener(this);
        player.setOnCompletionListener(this);
        player.setOnVideoSizeChangedListener(this);
        player.setOnInfoListener(this);
        videoIdText.setText(videoId);
        try {

            if (!isLocalPlay) {// 播放线上视频
                player.setVideoPlayInfo(videoId, ConfigUtil.USERID, ConfigUtil.API_KEY, verificationCode, this);
                // 设置默认清晰度
                player.setDefaultDefinition(defaultDefinition);

            } else {// 播放本地已下载视频

                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

                if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                    path = Environment.getExternalStorageDirectory() + "/".concat(ConfigUtil.DOWNLOAD_DIR).concat("/").concat(videoId).concat(".mp4");
                    if (!new File(path).exists()) {
                        return;
                    }
                }
            }

        } catch (IllegalArgumentException e) {
            Log.e("player error", e.getMessage());
        } catch (SecurityException e) {
            Log.e("player error", e.getMessage());
        } catch (IllegalStateException e) {
            Log.e("player error", e + "");
        }

        //设置字幕监听器
        player.setOnSubtitleMsgListener(new OnSubtitleMsgListener() {
            @Override
            public void onSubtitleMsg(String url, String font, final int size, final String color, final String surroundColor, final double bottom, String code) {
                // 设置视频字幕
                subtitle = new Subtitle(new OnSubtitleInitedListener() {
                    @Override
                    public void onInited(Subtitle subtitle) {
                        // 初始化字幕控制菜单
                        // TODO 看看是否有问题
                    }
                });
                if (!TextUtils.isEmpty(url)) {
                    subtitle.initSubtitleResource(url);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (size > 0) {
                            subtitleText.setTextSize(size / 2);
                        }
                        try {
                            if (!TextUtils.isEmpty(color)) {
                                String newColor = color;
                                if (color.contains("0x")) {
                                    newColor = color.replace("0x", "#");
                                }
                                subtitleText.setTextColor(Color.parseColor(newColor));
                                subtitleText.setShadowLayer(10F, 5F, 5F, Color.YELLOW);
                            }

                            if (!TextUtils.isEmpty(surroundColor)) {
                                String newSurroundColor = surroundColor;
                                if (surroundColor.contains("0x")) {
                                    newSurroundColor = surroundColor.replace("0x", "#");
                                }
                                subtitleText.setShadowLayer(10F, 5F, 5F, Color.parseColor(newSurroundColor));
                            }
                        } catch (Exception e) {

                        }

                        if (bottom > 0) {
                            int paddingBottom = 0;
                            Resources resources = getResources();
                            if (resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                paddingBottom = (int) (resources.getDisplayMetrics().heightPixels * bottom);
                            } else {
                                paddingBottom = (int) (resources.getDisplayMetrics().widthPixels * bottom);
                            }
                            subtitleText.setPadding(0, 0, 0, paddingBottom);
                        }

                        //设置字体  将字体文件拷贝到assets文件夹下
//                        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/roli.ttc");
                    }
                });

            }
        });

    }

    private void initTimerTask() {
        if (timerTask != null) {
            timerTask.cancel();
        }

        timerTask = new TimerTask() {
            @Override
            public void run() {

                if (!isPrepared) {
                    return;
                }

                playerHandler.sendEmptyMessage(0);
            }
        };
        timer.schedule(timerTask, 0, 1000);

    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        initTimerTask();
        isPrepared = true;
        if (!isFreeze) {
            if (isPlaying == null || isPlaying.booleanValue()) {
                player.start();
                ivPlay.setImageResource(R.drawable.smallstop_ic);
            } else {
                player.start();
                player.pause();
            }
        }

        if (!isLocalPlay) {
            if (currentPosition > 0) {
                player.seekTo(currentPosition);
            } else {
                //修复Bug 视频播放时，点击图片广告后，返回视频，视频从头播放
                if (isClickPauseAd) {
                    player.seekTo(currentPlayPosition);
                    isClickPauseAd = false;
                } else {
                    lastPlayPosition = DataSet.getVideoPosition(videoId);
                    if (lastPlayPosition > 0) {
                        player.seekTo(lastPlayPosition);
                    }
                }

            }
        }

        definitionMap = player.getDefinitions();
        if (!isLocalPlay) {
            initDefinitionPopMenu();
        }

        bufferProgressBar.setVisibility(View.GONE);
        setSurfaceViewLayout();
        videoDuration.setText(ParamsUtil.millsecondsToStr(player.getDuration()));
    }

    // 设置surfaceview的布局
    private void setSurfaceViewLayout() {
        LayoutParams params = PlayerUtil.getScreenSizeParams(wm, screenSizeArray[currentScreenSizeFlag],
                player.getVideoWidth(), player.getVideoHeight());
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        textureView.setLayoutParams(params);
    }

    private void initDefinitionPopMenu() {
        if (definitionMap.size() > 1) {
            currentDefinitionIndex = 1;
            Integer[] definitions = new Integer[]{};
            definitions = definitionMap.values().toArray(definitions);
            // 设置默认为普清，所以此处需要判断一下
            for (int i = 0; i < definitions.length; i++) {
                if (definitions[i].intValue() == defaultDefinition) {
                    currentDefinitionIndex = i;
                }
            }

//			firstInitDefinition = false;
        }

        definitionMenu = new PopMenu(this, R.drawable.popdown, currentDefinitionIndex, getResources().getDimensionPixelSize(R.dimen.popmenu_height));
        // 设置清晰度列表
        definitionArray = new String[]{};
        definitionArray = definitionMap.keySet().toArray(definitionArray);

        definitionMenu.addItems(definitionArray);
        definitionMenu.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(int position) {
                try {

                    currentDefinitionIndex = position;
                    defaultDefinition = definitionMap.get(definitionArray[position]);

                    if (isPrepared) {
                        currentPosition = player.getCurrentPosition();
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
                    Log.e("player error", e.getMessage());
                }

            }
        });
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        skbProgress.setSecondaryProgress(percent);
    }

    OnClickListener onClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            resetHideDelayed();

            switch (v.getId()) {
                case R.id.iv_ad_back:
                case R.id.backPlayList:
                    if (PlayerUtil.isPortrait() || isLocalPlay) {
                        finish();
                    } else {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                    break;
                case R.id.iv_ad_fullscreen:
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
                        toastInfo("已解开屏幕");
                    } else {
                        lockView.setSelected(true);
                        PlayerUtil.setLandScapeRequestOrientation(wm, ADMediaPlayActivity.this);
                        setLayoutVisibility(View.GONE, true);
                        lockView.setVisibility(View.VISIBLE);
                        toastInfo("已锁定屏幕");
                    }
                    break;
                case R.id.iv_center_play:
                case R.id.iv_play:
                    changePlayStatus();
                    break;
                case R.id.iv_download_play:
                    downloadCurrentVideo();
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
            }
        }
    };

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
        isSwitchVideo = true;
        if (isCompleted) {
            updateCompleteDataPosition();
        } else {
            updateDataPosition();
        }

        isPrepared = false;
        setLayoutVisibility(View.GONE, false);
        bufferProgressBar.setVisibility(View.VISIBLE);
        ivCenterPlay.setVisibility(View.GONE);

        currentPosition = 0;
        currentPlayPosition = 0;

        timerTask.cancel();

        videoId = PlayFragment.playVideoIds[position];

        if (playChangeVideoPopupWindow != null) {
            playChangeVideoPopupWindow.setSelectedPosition(getCurrentVideoIndex()).refreshView();
        }

        player.pause();
        player.stop();
        player.setDefaultDefinition(defaultDefinition);
        player.setVideoPlayInfo(videoId, ConfigUtil.USERID, ConfigUtil.API_KEY, verificationCode, ADMediaPlayActivity.this);
        getAdInfo();

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
                        currentScreenSizeFlag = 0;
                        subtitleText.setVisibility(View.VISIBLE);
                        break;
                    case R.id.rb_subtitle_close:// 关闭字幕
                        currentScreenSizeFlag = 1;
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

                Toast.makeText(getApplicationContext(), screenSizeArray[position], Toast.LENGTH_SHORT).show();
                currentScreenSizeFlag = position;
                setSurfaceViewLayout();
            }
        });

    }

    private void downloadCurrentVideo() {
        if (DataSet.hasDownloadInfo(videoId)) {
            Toast.makeText(this, "文件已存在", Toast.LENGTH_SHORT).show();
            return;
        }

        DownloadController.insertDownloadInfo(videoId, verificationCode, videoId);
        Toast.makeText(this, "文件已加入下载队列", Toast.LENGTH_SHORT).show();
    }

    private void toastInfo(String info) {
        Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
    }

    HotspotSeekBar.OnSeekBarChangeListener hotspotOnSeekBarChangeListener = new HotspotSeekBar.OnSeekBarChangeListener() {
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
            playerHandler.postDelayed(hidePlayRunnable, 5000);
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
     * @param visibility 显示状态
     * @param isDisplay  是否延迟消失
     */
    private void setLayoutVisibility(int visibility, boolean isDisplay) {
        if (player == null || player.getDuration() <= 0) {
            return;
        }

        playerHandler.removeCallbacks(hidePlayRunnable);

        this.isDisplay = isDisplay;

        if (definitionMenu != null && visibility == View.GONE) {
            definitionMenu.dismiss();
        }

        if (isDisplay) {
            playerHandler.postDelayed(hidePlayRunnable, 5000);
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
            toastInfo("视频异常，无法播放。");
            super.handleMessage(msg);
        }

    };

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Message msg = new Message();
        msg.what = what;
        if (alertHandler != null) {
            alertHandler.sendMessage(msg);
        }
        return true; //如果是false，则会回调onComplete
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        setSurfaceViewLayout();
    }


    // 重置隐藏界面组件的延迟时间
    private void resetHideDelayed() {
        playerHandler.removeCallbacks(hidePlayRunnable);
        playerHandler.postDelayed(hidePlayRunnable, 5000);
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
            if (lockView.isSelected()) {
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
                parseAudioScroll(distanceY);
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

            skbProgress.setProgress((int) currentPosition, player.getDuration());
        }

        private void parseAudioScroll(float distanceY) {
            if (!isDisplay) {
                setLayoutVisibility(View.VISIBLE, true);
            }
            scrollTotalDistance += distanceY;

            float height = wm.getDefaultDisplay().getHeight() * 0.75f;
            // 上滑distanceY为正
            currentVolume = (int) (scrollCurrentVolume + maxVolume * scrollTotalDistance / height);

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
            if (lockView.isSelected()) {
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
            if (isDisplay) {
                setLayoutVisibility(View.GONE, false);
            } else {
                setLayoutVisibility(View.VISIBLE, true);
            }
            return super.onSingleTapConfirmed(e);
        }
    }

    private void changePlayStatus() {
        if (player.isPlaying()) {
            player.pause();
            ivCenterPlay.setVisibility(View.VISIBLE);
            ivPlay.setImageResource(R.drawable.smallbegin_ic);
            showPauseAd();

        } else {
            player.start();
            ivCenterPlay.setVisibility(View.GONE);
            ivPlay.setImageResource(R.drawable.smallstop_ic);
            hidePauseAd();
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case DWMediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (player.isPlaying()) {
                    bufferProgressBar.setVisibility(View.VISIBLE);
                }
                break;
            case DWMediaPlayer.MEDIA_INFO_BUFFERING_END:
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

        if (!lockView.isSelected() && (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)) {
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
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onBackPressed() {

        if (PlayerUtil.isPortrait() || isLocalPlay) {
            super.onBackPressed();
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        isOnPause = false;
        if (adCount > -1 && frontADInfo != null && rlAd != null) {

            if (skipTime > 0) {
                startAdCloseTimerTask();
            }

            startAdCountDown();
            if (isVideoAd) {
                try {
                    adPlayer.start();
                } catch (Exception e) {
                }
            }
            return;
        }

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
        if (!isLocalPlay) {
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        isOnPause = true;

        if (skipTime > 0) {
            stopAdCloseTimerTask();
        }

        if (adCount > -1) {
            stopAdCountDown();
            if (isVideoAd) {
                try {
                    adPlayer.pause();
                } catch (Exception e) {
                }
            }

            return;
        }

        if (isPrepared) {
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
    }

    @Override
    protected void onStop() {
        if (!isLocalPlay) {
            sensorManager.unregisterListener(this);
            PlayerUtil.setLandScapeRequestOrientation(wm, this);
        }
        super.onStop();
    }

    private void updateDataPosition() {
        if (isLocalPlay) {
            return;
        }

        if (currentPlayPosition > 0) {
            if (DataSet.getVideoPosition(videoId) > 0) {
                DataSet.updateVideoPosition(videoId, currentPlayPosition);
            } else {
                DataSet.insertVideoPosition(videoId, currentPlayPosition);
            }
        }
    }

    private void updateCompleteDataPosition() {
        if (DataSet.getVideoPosition(videoId) > 0) {
            DataSet.updateVideoPosition(videoId, currentPlayPosition);
        } else {
            DataSet.insertVideoPosition(videoId, currentPlayPosition);
        }
    }

    @Override
    protected void onDestroy() {
        if (timerTask != null) {
            timerTask.cancel();
        }

        if (adTimer != null) {
            adTimer.cancel();
        }
        playerHandler.removeCallbacksAndMessages(null);
        playerHandler = null;

        alertHandler.removeCallbacksAndMessages(null);
        alertHandler = null;

        if (currentPlayPosition > 0) {
            if (lastPlayPosition > 0) {
                DataSet.updateVideoPosition(videoId, currentPlayPosition);
            } else {
                DataSet.insertVideoPosition(videoId, currentPlayPosition);
            }
        }

        if (player != null) {
            player.reset();
            player.release();
            player = null;
        }
        if (dialog != null) {
            dialog.dismiss();
        }
        if (!isLocalPlay) {
            networkInfoTimerTask.cancel();
        }

        if (mDWMediaAD != null) {
            mDWMediaAD.stopFrontAD();
            mDWMediaAD.stopPauseAD();
        }

        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);

        if (isPrepared) {
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

            if (ivAdFullscreen != null) {
                ivAdFullscreen.setImageResource(R.drawable.fullscreen_close);
            }

        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            rlBelow.setVisibility(View.GONE);
            ivFullscreen.setImageResource(R.drawable.fullscreen_open);
            if (ivAdFullscreen != null) {
                ivAdFullscreen.setImageResource(R.drawable.fullscreen_open);
            }
        }

        setSurfaceViewLayout();

        if (rlAdPause != null && rlAdPause.isShown()) {
            showPauseAd();
        }

        PlayerUtil.resizeAdView(ADMediaPlayActivity.this, wm, ivAd, frontWidth, frontHeight);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        if (isLocalPlay) {
            toastInfo("播放完成！");
            finish();
            return;
        }

        if (isPrepared) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    toastInfo("切换中，请稍候……");
                    currentPlayPosition = 0;
                    currentPosition = 0;
                    changeToNextVideo(true);
                }
            });
        }
    }

    // 广告handlerstatic
    private class ADHandler extends Handler {
        private WeakReference<ADMediaPlayActivity> wReference;

        public ADHandler(ADMediaPlayActivity activity) {
            wReference = new WeakReference<ADMediaPlayActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ADMediaPlayActivity activity = wReference.get();
            if (activity == null) {
                return;
            }

            switch (msg.what) {
                case 0:
                    activity.initFrontAdView();
                    break;
                case 1:
                    activity.initAdPauseView();
                    break;
            }
            super.handleMessage(msg);
        }
    }

    ;

    private ADHandler adHandler;
    private PauseADInfo pauseADInfo;
    private FrontADInfo frontADInfo;
    private int adCount = 100;
    private boolean isVideoAd = true;
    private DWMediaADListener dwMediaADlistener = new DWMediaADListener() {
        @Override
        public void onPauseAD(PauseADInfo info) {
            pauseADInfo = info;
            adHandler.sendEmptyMessage(1);
        }

        @Override
        public void onFrontAD(FrontADInfo info) {
            frontADInfo = info;
            adCount = frontADInfo.getTime();


            isVideoAd = frontADInfo.isVideo();

            adHandler.sendEmptyMessage(0);

        }

        @Override
        public void onFrontADError(DreamwinException e) {
            hideAd();
        }

        @Override
        public void onPauseADError(DreamwinException e) {
        }
    };

    private DWMediaAD mDWMediaAD;

    // 获取广告信息
    private void getAdInfo() {
        if (!isLocalPlay) {
            adHandler = new ADHandler(this);
            mDWMediaAD = new DWMediaAD(dwMediaADlistener, ConfigUtil.USERID, videoId);
            mDWMediaAD.getFrontAD();
            mDWMediaAD.getPauseAD();
        } else {
            adCount = -1;
        }
    }


    private ImageView tvPauseCloseView;
    private RelativeLayout rlAdPause;
    private ImageView ivPauseView;

    private OnClickListener adPauseOnClickListener;
    private int pauseWidth, pauseHeight;

    private void showPauseAd() {
        if (rlAdPause != null) {
            PlayerUtil.resizeAdView(ADMediaPlayActivity.this, wm, ivPauseView, pauseWidth, pauseHeight);
            rlAdPause.setVisibility(View.VISIBLE);
        }
    }

    private void hidePauseAd() {
        if (rlAdPause != null) {
            rlAdPause.setVisibility(View.GONE);
        }
    }

    private void initAdPauseView() {
        adPauseOnClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.iv_ad_pause_close:
                        hidePauseAd();
                        break;
                    case R.id.iv_ad_pause:
                        String clickUrl = pauseADInfo.getAd().get(0).getClickurl();
                        if (!clickUrl.startsWith("http")) {
                            return;
                        }

                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(clickUrl));
                        startActivity(intent);
                        //修复Bug,视频播放时，点击图片广告后，返回视频，视频从头播放
                        isClickPauseAd = true;
                        break;
                }
            }
        };

        rlAdPause = (RelativeLayout) findViewById(R.id.rl_ad_pause);
        tvPauseCloseView = (ImageView) findViewById(R.id.iv_ad_pause_close);
        tvPauseCloseView.setOnClickListener(adPauseOnClickListener);

        ivPauseView = (ImageView) findViewById(R.id.iv_ad_pause);
        ivPauseView.setOnClickListener(adPauseOnClickListener);
        setPauseImageFromUrl();
    }

    // 获取图片并应用到暂停的ImageView控件上
    private void setPauseImageFromUrl() {
        Picasso.with(this).load(pauseADInfo.getAd().get(0).getMaterial())
                .skipMemoryCache().transform(new Transformation() {

            @Override
            public Bitmap transform(Bitmap arg0) {
                pauseHeight = arg0.getHeight();
                pauseWidth = arg0.getWidth();
                PlayerUtil.resizeAdView(ADMediaPlayActivity.this, wm, ivPauseView, pauseWidth, pauseHeight);
                return arg0;
            }

            @Override
            public String key() {
                return "pause";
            }
        }).into(ivPauseView);
    }

    private TextView tvCountDown, tvAdInfo, tvAdClose;
    private RelativeLayout rlAd;
    private ImageView ivAd, ivAdBack, ivAdFullscreen;

    private void initFrontAdView() {
        rlAd = (RelativeLayout) findViewById(R.id.rl_ad);
        ivAdBack = (ImageView) findViewById(R.id.iv_ad_back);
        ivAdBack.setOnClickListener(onClickListener);
        tvCountDown = (TextView) findViewById(R.id.tv_countdown);
        tvAdInfo = (TextView) findViewById(R.id.tv_adinfo);
        ivAd = (ImageView) findViewById(R.id.iv_ad);
        ivAdFullscreen = (ImageView) findViewById(R.id.iv_ad_fullscreen);
        ivAdFullscreen.setOnClickListener(onClickListener);

        adUriString = frontADInfo.getAd().get(0).getClickurl();

        if (isVideoAd) {
            List<AdBean> lists = frontADInfo.getAd();
            int size = lists.size();
            adPlayUrls = new String[size];
            for (int i = 0; i < size; i++) {
                adPlayUrls[i] = lists.get(i).getMaterial();
            }

            ivAd.setVisibility(View.GONE);
            try {
                initAdPlayer(surface);
            } catch (Exception e) {

            }
        } else {
            ivAd.setVisibility(View.VISIBLE);
            setFrontImageFromUrl();
        }

        initAdClickListener();

        if (isOnPause) {
            return;
        } else {
            startAdCountDown();
        }

        bufferProgressBar.setVisibility(View.GONE);
    }

    private void setFrontImageFromUrl() {
        Picasso.with(this).load(frontADInfo.getAd().get(0).getMaterial())
                .skipMemoryCache().transform(new Transformation() {
            @Override
            public Bitmap transform(Bitmap arg0) {
                frontHeight = arg0.getHeight();
                frontWidth = arg0.getWidth();
                PlayerUtil.resizeAdView(ADMediaPlayActivity.this, wm, ivAd, frontWidth, frontHeight);
                return arg0;
            }

            @Override
            public String key() {
                return "front";
            }
        }).into(ivAd);
    }

    private int frontHeight, frontWidth;

    private OnClickListener adStartOnclickListener;

    private String adUriString = "";

    private void initAdClickListener() {
        adStartOnclickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.tv_adinfo:
                    case R.id.playerSurfaceView:
                    case R.id.iv_ad:
                        showFrontAdInfo();
                        break;
                    case R.id.tv_close_ad:
                        hideAd();
                        break;
                }
            }
        };

        tvAdInfo.setOnClickListener(adStartOnclickListener);

        if (adUriString.startsWith("http")) {
            tvAdInfo.setVisibility(View.VISIBLE);
        } else {
            tvAdInfo.setVisibility(View.INVISIBLE);
        }

        if (frontADInfo.getCanclick() == 1) {
            if (isVideoAd) {
                textureView.setOnClickListener(adStartOnclickListener);
            } else {
                ivAd.setOnClickListener(adStartOnclickListener);
            }
        }

        if (frontADInfo.getCanskip() == 1) {
            skipTime = frontADInfo.getSkipTime();
            tvAdClose = (TextView) findViewById(R.id.tv_close_ad);
            tvAdClose.setVisibility(View.VISIBLE);

            if (skipTime == 0) {
                tvAdClose.setOnClickListener(adStartOnclickListener);
            } else {
                tvAdClose.setOnClickListener(null);
                startAdCloseTimerTask();
            }
        }
    }

    private int skipTime = 0;
    private TimerTask adCloseTimerTask;

    private void startAdCloseTimerTask() {
        if (adCloseTimerTask != null) {
            adCloseTimerTask.cancel();
        }

        adCloseTimerTask = new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (skipTime == 0) {
                            stopAdCloseTimerTask();
                            tvAdClose.setOnClickListener(adStartOnclickListener);
                            tvAdClose.setText("关闭广告");
                            return;
                        }
                        tvAdClose.setText(getAdSpannString(skipTime, "s后可关闭广告"));
                        skipTime--;
                    }
                });
            }
        };

        adTimer.schedule(adCloseTimerTask, 0, 1 * 1000);
    }

    private SpannableString getAdSpannString(int time, String lastStr) {
        String mTime = String.valueOf(time);
        SpannableString spannableString = new SpannableString(mTime + lastStr);
        ForegroundColorSpan span = new ForegroundColorSpan(0xffff643d);
        spannableString.setSpan(span, 0, mTime.length(), SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    private void stopAdCloseTimerTask() {
        if (adCloseTimerTask != null) {
            adCloseTimerTask.cancel();
        }
    }

    private void showFrontAdInfo() {
        if (!adUriString.startsWith("http")) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(adUriString));
        startActivity(intent);
    }

    private void showAd() {
        rlAd.setVisibility(View.VISIBLE);
    }

    private void hideAd() {
        if (rlAd != null) {
            rlAd.setVisibility(View.GONE);
        }
        adCount = -1;
        stopAdCountDown();
        if (adPlayers.size() > 0) {
            destroyAdPlayers();
        }

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                initSurfaceViewListener();
                bufferProgressBar.setVisibility(View.VISIBLE);
                player.reset();
                player.setSurface(surface);
                player.prepareAsync();
            }
        });

    }

    private void initSurfaceViewListener() {
        textureView.setOnClickListener(null);
        textureView.setOnTouchListener(mOntouchListener);
        initGesture();
    }

    private void initGesture() {
        rlPlay.setOnTouchListener(mOntouchListener);
        rlPlay.setClickable(true);
        rlPlay.setLongClickable(true);
        rlPlay.setFocusable(true);
    }

    private OnTouchListener mOntouchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!isPrepared) {
                return true;
            }

            resetHideDelayed();

            // 事件监听交给手势类来处理
            detector.onTouchEvent(event);
            return true;
        }
    };

    private Timer adTimer = new Timer();
    private TimerTask adTimerTask;

    private void startAdCountDown() {
        //修复Bug切换视频时广告不播放
        if (isSwitchVideo) {
            ViewGroup parent = (ViewGroup) textureView.getParent();
            parent.removeView(textureView);
            parent.addView(textureView, 0);
            isSwitchVideo = false;
        }
        showAd();

        if (adTimerTask != null) {
            adTimerTask.cancel();
        }
        adTimerTask = new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//						String ss = (adCount--) + "s";
                        tvCountDown.setText(getAdSpannString(adCount--, "s"));
                        if (adCount < 0) {
                            hideAd();
                        }
                    }
                });
            }
        };
        adTimer.schedule(adTimerTask, 0, 1000);
    }

    private void stopAdCountDown() {
        if (adTimerTask != null) {
            adTimerTask.cancel();
        }
    }

    private String[] adPlayUrls;
    private List<MediaPlayer> adPlayers = new ArrayList<MediaPlayer>();
    private MediaPlayer adPlayer;
    @SuppressLint("NewApi")
    private OnPreparedListener adOnPreparedListener = new OnPreparedListener() {

        @Override
        public void onPrepared(MediaPlayer mp) {
            adPlayer = mp;
            bufferProgressBar.setVisibility(View.GONE);
            mp.start();
            if (currentAdPosition > 0) {
                mp.seekTo(currentAdPosition);
            }
            int index = adPlayers.indexOf(mp);
            adUriString = frontADInfo.getAd().get(index).getClickurl();

            Log.e("demo", adUriString);
            if (index > -1) {
                adPosition = index;
            }
        }
    };


    private int currentAdPosition;
    private boolean isOnPause;
    private int adPosition = 0;

    private void initAdPlayer(Surface sh) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException {

        if (adPlayers.size() <= 0) {
            for (int i = 0; i < adPlayUrls.length; i++) {
                MediaPlayer player = new MediaPlayer();
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.setOnPreparedListener(adOnPreparedListener);
                player.setSurface(sh);
                player.setDataSource(adPlayUrls[i]);
                player.setScreenOnWhilePlaying(true);
                adPlayers.add(player);
                player.setOnVideoSizeChangedListener(this);
            }

            for (MediaPlayer player : adPlayers) {
                player.setOnCompletionListener(adOnCompletionListener);
            }
            adPlayers.get(adPosition).prepareAsync();

        }
    }

    private OnCompletionListener adOnCompletionListener = new OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer mp) {
            if (isSurfaceDestroy) {
                return;
            }

            currentAdPosition = 0;
            if (adPlayers.size() == 1) {
                mp.seekTo(0);
                mp.start();
            } else {
                mp.reset();
                int index = adPlayers.indexOf(mp);

                int currentIndex = ++index % adPlayers.size();
                MediaPlayer player = adPlayers.get(currentIndex);
                player.reset();
                player.setSurface(surface);
                try {
                    player.setDataSource(adPlayUrls[currentIndex]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                player.prepareAsync();
            }
        }
    };

    private void destroyAdPlayers() {
        for (MediaPlayer player : adPlayers) {
            player.reset();
            player.release();
        }

        adPlayers.clear();
    }
}