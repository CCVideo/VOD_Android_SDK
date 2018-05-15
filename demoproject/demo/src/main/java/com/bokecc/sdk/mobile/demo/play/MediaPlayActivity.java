package com.bokecc.sdk.mobile.demo.play;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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
import com.bokecc.sdk.mobile.demo.gif.GifMakerThread;
import com.bokecc.sdk.mobile.demo.gif.ProgressObject;
import com.bokecc.sdk.mobile.demo.gif.ProgressView;
import com.bokecc.sdk.mobile.demo.play.Subtitle.OnSubtitleInitedListener;
import com.bokecc.sdk.mobile.demo.play.controller.PlayerUtil;
import com.bokecc.sdk.mobile.demo.play.qa.Question;
import com.bokecc.sdk.mobile.demo.util.ConfigUtil;
import com.bokecc.sdk.mobile.demo.util.DataSet;
import com.bokecc.sdk.mobile.demo.util.MediaUtil;
import com.bokecc.sdk.mobile.demo.util.ParamsUtil;
import com.bokecc.sdk.mobile.demo.view.HotspotSeekBar;
import com.bokecc.sdk.mobile.demo.view.PlayChangeVideoPopupWindow;
import com.bokecc.sdk.mobile.demo.view.PlayTopPopupWindow;
import com.bokecc.sdk.mobile.demo.view.PopMenu;
import com.bokecc.sdk.mobile.demo.view.PopMenu.OnItemClickListener;
import com.bokecc.sdk.mobile.demo.view.QAView;
import com.bokecc.sdk.mobile.demo.view.VerticalSeekBar;
import com.bokecc.sdk.mobile.exception.DreamwinException;
import com.bokecc.sdk.mobile.play.DWMediaPlayer;
import com.bokecc.sdk.mobile.play.MediaMode;
import com.bokecc.sdk.mobile.play.OnDreamWinErrorListener;
import com.bokecc.sdk.mobile.play.OnHotspotListener;
import com.bokecc.sdk.mobile.play.OnPlayModeListener;
import com.bokecc.sdk.mobile.play.OnQAMsgListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

/**
 * 视频播放界面
 *
 * @author CC视频
 */
public class MediaPlayActivity extends Activity implements
        DWMediaPlayer.OnBufferingUpdateListener,
        DWMediaPlayer.OnInfoListener,
        DWMediaPlayer.OnPreparedListener, DWMediaPlayer.OnErrorListener,
        MediaPlayer.OnVideoSizeChangedListener, SensorEventListener, OnCompletionListener, TextureView.SurfaceTextureListener, OnDreamWinErrorListener {

    private static final String TAG = "PlayTag";
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

    private String path;

    private Boolean isPlaying;
    // 当player未准备好，并且当前activity经过onPause()生命周期时，此值为true
    private boolean isFreeze = false;
    private boolean isSurfaceDestroy = false;

    int currentPosition;
    private Dialog dialog;

    private String[] definitionArray;
    private final String[] screenSizeArray = new String[]{"满屏", "100%", "75%", "50%"};
    private final String[] subtitleSwitchArray = new String[]{"开启", "关闭"};
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

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        detector = new GestureDetector(this, new MyGesture());

        initView();

        initPlayHander();

        initPlayInfo();

        if (!isLocalPlay) {
            initNetworkTimerTask();
        }

        playDemoApplication.removeNotification();

    }

    ImageView lockView;
    ImageView ivCenterPlay;
    ImageView ivDownload;
    ImageView ivTopMenu;
    TextView tvChangeVideo;
    ImageView ivBackVideo, ivNextVideo, ivPlay;

    ImageView ivGifCreate;
    ImageView ivGifStop;
    ImageView ivGifShow;
    ProgressView gifProgressView;
    ProgressObject progressObject = new ProgressObject();
    TextView gifTips;
    TextView gifCancel;

    //========audio layout=====================

    LinearLayout avChangeLayout;
    TextView changeToVideoPlayView;
    TextView changeToAudioPlayView;

    private void initView() {

        rlBelow = (RelativeLayout) findViewById(R.id.rl_below_info);
        rlPlay = (RelativeLayout) findViewById(R.id.rl_play);
        rlPlay.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isPrepared || !isGifCancel || isAudio) {
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

        ivGifCreate = (ImageView) findViewById(R.id.iv_gif_create);
        ivGifCreate.setOnClickListener(onClickListener);

        ivGifStop = (ImageView) findViewById(R.id.iv_gif_stop);

        ivGifStop.setOnClickListener(onClickListener);

        ivGifShow = (ImageView) findViewById(R.id.gif_show);
        ivGifShow.setOnClickListener(onClickListener);

        gifProgressView = (ProgressView) findViewById(R.id.gif_progress_view);
        gifProgressView.setMaxDuration(gifMax);
        gifProgressView.setMinTime(gifMin);
        gifProgressView.setData(progressObject);

        gifTips = (TextView) findViewById(R.id.gif_tips);
        gifCancel = (TextView) findViewById(R.id.gif_cancel);
        gifCancel.setOnClickListener(onClickListener);

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
        audioSpeedView.setVisibility(View.INVISIBLE);

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

    //TODO gif最大时间，最小时间
    int gifMax = 15 * 1000;
    int gifMin = 3 * 1000;
    int gifIntervel = 200; //gif进度更新间隔

    private void initPlayHander() {
        playerHandler = new Handler() {
            public void handleMessage(Message msg) {

                if (player == null) {
                    return;
                }

                // 刷新字幕
                subtitleText.setText(subtitle.getSubtitleByTime(player.getCurrentPosition()));

                // 更新播放进度
                currentPlayPosition = player.getCurrentPosition();
                int duration = player.getDuration();

                if (duration > 0) {
                    String currentTime = ParamsUtil.millsecondsToMinuteSecondStr(player.getCurrentPosition());
                    playCurrentPosition.setText(currentTime);
                    audioCurrentTimeView.setText(currentTime);

                    long pos = audioSeekBar.getMax() * currentPlayPosition / duration;
                    audioSeekBar.setProgress((int)pos);

                    skbProgress.setProgress(currentPlayPosition, duration);
                }

                if (isQuestionTimePoint(currentPlayPosition) && (qaView == null || !qaView.isPopupWindowShown())) {
                    pauseVideoPlay();
                    showQuestion();
                }

                if (qaView != null && qaView.isPopupWindowShown()) {
                    player.pauseWithoutAnalyse(); //针对有的手机上无法暂停，反复调用pause()
                }
            }
        };
    }

    private MediaMode currentPlayMode = null;

    PlayDemoApplication playDemoApplication;

    TreeMap<Integer, String> hotspotMap;

    TreeMap<Integer, Question> questions;

    private void initPlayInfo() {

        // 通过定时器和Handler来更新进度
        isPrepared = false;
        player = playDemoApplication.getDWPlayer();

        videoId = getIntent().getStringExtra("videoId");
        videoIdText.setText(videoId);
        isLocalPlay = getIntent().getBooleanExtra("isLocalPlay", false);
        int playModeInteger = getIntent().getIntExtra("playMode", 1);

        boolean isFromNotify = getIntent().getBooleanExtra("isFromNotify", false);

        if (isFromNotify) {
            currentPosition = player.getCurrentPosition();
        }

        player.clearMediaData();

        player.reset();
        player.setOnDreamWinErrorListener(this);
        player.setOnErrorListener(this);
        player.setOnCompletionListener(this);
        player.setOnVideoSizeChangedListener(this);
        player.setOnInfoListener(this);
        player.setDefaultPlayMode(MediaUtil.PLAY_MODE, new OnPlayModeListener() {
            @Override
            public void onPlayMode(MediaMode playMode) {
                currentPlayMode = playMode;
            }
        });

        player.setOnHotspotListener(new OnHotspotListener() {
            @Override
            public void onHotspots(TreeMap<Integer, String> hotspotMap) {
                MediaPlayActivity.this.hotspotMap = hotspotMap;
            }
        });

        player.setOnQAMsgListener(new OnQAMsgListener() {

            @Override
            public void onQAMessage(JSONArray qaJsonArray) {
                // 现在处理方式为只要是答过的题，就不再回答了
                if (questions == null) {
                    questions = new TreeMap<>();
                    createQuestionMap(qaJsonArray);
                }
            }
        });

        player.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
                if (qaView != null && qaView.isPopupWindowShown()) {
                    player.pauseWithoutAnalyse();
                }
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
                // 设置默认清晰度
                player.setDefaultDefinition(defaultDefinition);

            } else {// 播放本地已下载视频
                player.setVideoPlayInfo(null, null, null, this);
                if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                    path = Environment.getExternalStorageDirectory() + "/".concat(ConfigUtil.DOWNLOAD_DIR).concat("/").concat(videoId).concat(suffix);
                    if (!new File(path).exists()) {
                        return;
                    }
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
                // TODO 看看是否有问题
            }
        });
        subtitle.initSubtitleResource(subtitleExampleURL);

    }

    private void createQuestionMap(JSONArray qaJsonArray) {
        for (int i=0; i < qaJsonArray.length(); i++) {
            try {
                Question question = new Question(qaJsonArray.getJSONObject(i));
                questions.put(question.getShowTime(), question);
            } catch (JSONException e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        }
    }

    int gifVideoWidth;
    int gifVideoHeight;
    @Override
    public void onPrepared(MediaPlayer mp) {
        if (this.isDestroyed()) {
            return;
        }

        isPrepared = true;
        if (!isFreeze) {
            if (isPlaying == null || isPlaying.booleanValue()) {
                startvideoPlay();
            } else {
                // 解决在qa展示时，一直报错的bug
                player.start();
                player.pause();
            }
        }

        if (!isLocalPlay) {
            if (currentPosition > 0) {
                player.seekTo(currentPosition);
            } else {
                lastPlayPosition = DataSet.getVideoPosition(videoId);
                if (lastPlayPosition > 0) {
                    seekTo(lastPlayPosition);
                }
            }
        }

        definitionMap = player.getDefinitions();
        if (!isLocalPlay) {
            initDefinitionPopMenu();
        }

        bufferProgressBar.setVisibility(View.GONE);
        setSurfaceViewLayout();

        String videoDurationStr = ParamsUtil.millsecondsToMinuteSecondStr(player.getDuration());
        videoDuration.setText(videoDurationStr);
        audioDurationView.setText(videoDurationStr);

        int mVideoWidth = player.getVideoWidth();
        int mVideoHeight = player.getVideoHeight();

        //设置生成gif图片的分辨率
        if (mVideoWidth > 320) {
            gifVideoWidth = 320;
            float ratio = mVideoWidth / 320.0f;
            gifVideoHeight = (int) (mVideoHeight / ratio);
        } else {
            gifVideoWidth = mVideoWidth;
            gifVideoHeight = mVideoHeight;
        }

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

        initTimerTask();
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
                    Log.e(TAG, e.getMessage());
                }

            }
        });
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
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
                        PlayerUtil.toastInfo(MediaPlayActivity.this, "已解开屏幕");
                    } else {
                        lockView.setSelected(true);
                        PlayerUtil.setLandScapeRequestOrientation(wm, MediaPlayActivity.this);
                        setLayoutVisibility(View.GONE, true);
                        lockView.setVisibility(View.VISIBLE);
                        PlayerUtil.toastInfo(MediaPlayActivity.this, "已锁定屏幕");
                    }
                    break;
                case R.id.iv_center_play:
                case R.id.iv_play:
                    changePlayStatus();
                    break;
                case R.id.iv_download_play:
                    PlayerUtil.downloadCurrentVideo(MediaPlayActivity.this, videoId, isAudio);
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
                case R.id.iv_gif_create:
                    startCreateGif();
                    break;
                case R.id.iv_gif_stop:
                    stopGif();
                    break;
                case R.id.gif_cancel:
                    cancelGif();
                    break;
                case R.id.gif_show:
                    shareGif();
                    break;
                case R.id.change_audio_play:
                    cancelTimerTask();
                    PlayerUtil.toastInfo(MediaPlayActivity.this, "切换到音频播放，请稍候……");
                    changeAudioPlayLayout();
                    prepareAVPlayer(true);
                    break;
                case R.id.change_video_play:
                    cancelTimerTask();
                    PlayerUtil.toastInfo(MediaPlayActivity.this, "切换到视频播放，请稍候……");
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
            }
        }
    };

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
            player.setOnBufferingUpdateListener(this);
            player.setOnPreparedListener(this);
            player.setSurface(surface);
            player.setScreenOnWhilePlaying(true);

            if (isLocalPlay) {
                player.setDataSource(path);
            }
            player.setHttpsPlay(false);
            player.prepareAsync();
        } catch (Exception e) {
            Log.e(TAG, "error", e);
        }
        Log.i(TAG, "surface created");

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        if (player == null) {
            return false;
        }
        if (isPrepared) {
            currentPosition = player.getCurrentPosition();
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

    long lastTimeMillis;

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        if (isGifStart) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastTimeMillis > 100) {
                Bitmap bitmap = textureView.getBitmap(gifVideoWidth, gifVideoHeight);
                gifMakerThread.addBitmap(bitmap);
                lastTimeMillis = currentTime;
            }
        }
    }

    @Override
    public void onPlayError(final DreamwinException e) {
        PlayerUtil.toastInfo(this, e.getMessage());
        player.setHttpsPlay(false);
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

            initTimerTask();
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
                AlertDialog.Builder builder = new AlertDialog.Builder(MediaPlayActivity.this);
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
        PlayerUtil.toastInfo(this, "当前无网络信号，无法播放");
    }

    private void initTimerTask() {
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
        timer.schedule(timerTask, 0, 1000);
    }

    private void prepareAVPlayer(boolean isAudio) {
        if (isPrepared) {
            currentPosition = player.getCurrentPosition();
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
        // 切换视频时，清除questions的内容
        if (questions != null) {
            questions.clear();
            questions = null;
        }

        // 切换视频时，清除打点信息
        if (hotspotMap != null) {
            hotspotMap.clear();
            skbProgress.clearHotSpots();
        }

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
        player.setVideoPlayInfo(videoId, ConfigUtil.USERID, ConfigUtil.API_KEY, MediaPlayActivity.this);
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

                PlayerUtil.toastInfo(MediaPlayActivity.this, screenSizeArray[position]);
                currentScreenSizeFlag = position;

                setSurfaceViewLayout();
            }
        });

    }

    OnSeekBarChangeListener audioPlayOnSeekBarChangeListener = new OnSeekBarChangeListener() {
        int progress = 0;

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if ((networkConnected || isLocalPlay) && isPrepared) {
                player.seekTo(progress);
                playerHandler.postDelayed(hidePlayRunnable, 5000);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            playerHandler.removeCallbacks(hidePlayRunnable);
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if ((networkConnected || isLocalPlay) && isPrepared) {
                this.progress = progress * player.getDuration() / seekBar.getMax();
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
                seekTo(progress);
                playerHandler.postDelayed(hidePlayRunnable, 5000);
            }
        }

    };

    OnSeekBarChangeListener volumeSeekBarChangeListener = new OnSeekBarChangeListener() {

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
     * @param visibility 显示状态
     * @param isDisplay  是否延迟消失
     */
    private void setLayoutVisibility(int visibility, boolean isDisplay) {
        if (player == null || !isGifCancel || isAudio) {
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
            ivGifCreate.setVisibility(View.GONE);
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
            ivGifCreate.setVisibility(visibility);
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
            PlayerUtil.toastInfo(MediaPlayActivity.this, "视频异常，无法播放。");
            super.handleMessage(msg);
        }

    };

    boolean isBackupPlay = false;

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {

        if (this.isDestroyed()) {
            return true;
        }

        Message msg = new Message();
        msg.what = what;

        if (!isBackupPlay && !isLocalPlay) {
            startBackupPlay();
        } else {
            if (alertHandler != null) {
                alertHandler.sendMessage(msg);
            }
        }
        return true;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        if (this.isDestroyed()) {
            return;
        }
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

//            player.seekTo((int) currentPosition);
            seekTo((int) currentPosition);
            playCurrentPosition.setText(ParamsUtil.millsecondsToMinuteSecondStr((int) currentPosition));

            int pos = (int) (audioSeekBar.getMax() * currentPosition / duration);
            audioSeekBar.setProgress(pos);

            skbProgress.setProgress((int)currentPosition, player.getDuration());
        }

        private void parseSoundScroll(float distanceY) {
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

    private Runnable backupPlayRunnable = new Runnable() {

        @Override
        public void run() {
            startBackupPlay();
        }
    };

    private void startBackupPlay() {
        cancelTimerTask();
        player.setBackupPlay(true);
        isBackupPlay = true;
        player.reset();
        player.prepareAsync();
    }

    private void cancelTimerTask() {
        if (timerTask != null) {
            timerTask.cancel();
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (this.isDestroyed()) {
            return false;
        }

        switch (what) {
            case DWMediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (player.isPlaying()) {
                    bufferProgressBar.setVisibility(View.VISIBLE);
                }

                if (!isBackupPlay) {
                    playerHandler.postDelayed(backupPlayRunnable, 10 * 1000);
                }

                break;
            case DWMediaPlayer.MEDIA_INFO_BUFFERING_END:
                bufferProgressBar.setVisibility(View.GONE);
                playerHandler.removeCallbacks(backupPlayRunnable);

                if (qaView != null && qaView.isPopupWindowShown()) {
                    player.pauseWithoutAnalyse();
                }

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

        if (!isAudio && isGifCancel && !lockView.isSelected() && (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)) {
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

        if (!isGifCancel) {
            cancelGif();
            return;
        }

        if (PlayerUtil.isPortrait() || isLocalPlay) {
            super.onBackPressed();
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
    protected void onStart() {
        super.onStart();
        if (qaView != null) {
            qaView.setQAViewDismissListener(myQAViewDismissListener);
        }
    }

    @Override
    protected void onStop() {

        if (qaView != null && qaView.isPopupWindowShown()) {
            qaView.setQAViewDismissListener(null);
            qaView.dismiss();
        }

        sensorManager.unregisterListener(this);

        super.onStop();
    }

    private void updateCurrentDataPosition() {
        if (!isLocalPlay && currentPlayPosition > 0) {
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

        if (isAudio && isPrepared) {
            showNotification();
        }

        playerHandler.removeCallbacksAndMessages(null);
        playerHandler = null;

        alertHandler.removeCallbacksAndMessages(null);
        alertHandler = null;

        updateCurrentDataPosition();

        if (!isAudio) {
            playDemoApplication.releaseDWPlayer();
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

        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            rlBelow.setVisibility(View.GONE);
            ivFullscreen.setImageResource(R.drawable.fullscreen_open);

            skbProgress.setHotspotShown(true);
        }

        setSurfaceViewLayout();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (this.isDestroyed()) {
            return;
        }

        if (isLocalPlay) {
            PlayerUtil.toastInfo(MediaPlayActivity.this, "播放完成！");
            finish();
            return;
        }

        if (isPrepared) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    PlayerUtil.toastInfo(MediaPlayActivity.this, "切换下一个视频，请稍候……");
                    currentPlayPosition = 0;
                    currentPosition = 0;
                    changeToNextVideo(true);
                }
            });
        }
    }

    //====================== gif create===================

    boolean isGifStart = false;
    boolean isGifCancel = true;
    boolean isGifFinish = false;

    GifMakerThread gifMakerThread;

    private void startCreateGif() {

        if (gifMakerThread != null && gifMakerThread.isAlive()) {
            PlayerUtil.toastInfo(this, "处理中，请稍候");
            return;
        }

        gifFile = new File(Environment.getExternalStorageDirectory().getPath() + "/CCDownload/" + System.currentTimeMillis() + ".gif");

        startvideoPlay();

        ivGifShow.setImageBitmap(null);
        setLayoutVisibility(View.GONE, false);
        PlayerUtil.setLandScapeRequestOrientation(wm, this);
        ivGifStop.setImageResource(R.drawable.gif_disble);
        setGifViewStatus(View.VISIBLE);
        gifTips.setText("录制3s，即可分享");

        isGifStart = true;
        isGifCancel = false;
        isGifFinish = false;

        lastTimeMillis = 0;
        gifRecordTime = 0;

        startGifTimerTask();

        progressObject.setDuration(0);

        gifMakerThread = new GifMakerThread(gifMakerListener, gifFile.getAbsolutePath(), 100, 0);
        gifMakerThread.startGif();
    }

    private void shareGif() {
        if (!isGifFinish) {
            return;
        }

        Uri imageUri = Uri.fromFile(gifFile);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent, "分享到"));
    }

    // 停止获取新的gif帧
    private void stopGif() {
        if (gifRecordTime < gifMin) {
            return;
        }

        endCreateGif();
    }

    // 增加这个方法，在cancelGif的时候直接调用
    private void endCreateGif() {

        if (isGifStart) {
            isGifStart = false;
            if (gifMakerThread != null) {
                gifMakerThread.stopGif();
            }

            stopGifTimerTask();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    gifTips.setText("制作中，请等待...");
                }
            });

        }
    }

    // 取消gif的制作
    private void cancelGif() {
        endCreateGif();
        gifMakerThread.cancelGif();

        isGifCancel = true;

        setGifViewStatus(View.GONE);
        setLayoutVisibility(View.VISIBLE, true);

        startvideoPlay();
    }

    File gifFile;

    GifMakerThread.GifMakerListener gifMakerListener = new GifMakerThread.GifMakerListener() {

        @Override
        public void onGifFinish() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isGifFinish = true;
                    pauseVideoPlay();
                    gifTips.setText("制作完成，点击图片分享");
                    ivGifShow.setVisibility(View.VISIBLE);

                    Glide.with(MediaPlayActivity.this).load(gifFile).asGif().diskCacheStrategy(DiskCacheStrategy.NONE).into(ivGifShow);
                }
            });

        }

        @Override
        public void onGifError(final Exception e) {
            PlayerUtil.toastInfo(MediaPlayActivity.this, "" + e.getLocalizedMessage());
        }
    };

    private void setGifViewStatus(int status) {
        ivGifStop.setVisibility(status);
        gifProgressView.setVisibility(status);
        gifTips.setVisibility(status);
        gifCancel.setVisibility(status);
        ivGifShow.setVisibility(status);
    }

    TimerTask gifCreateTimerTask;
    int gifRecordTime = 0;

    private void startGifTimerTask() {
        stopGifTimerTask();

        gifCreateTimerTask = new TimerTask() {
            @Override
            public void run() {

                gifRecordTime = gifRecordTime + gifIntervel;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        progressObject.setDuration(gifRecordTime);

                        if (gifRecordTime >= gifMin) {
                            ivGifStop.setImageResource(R.drawable.gif_enable);
                            gifTips.setText("点击停止，即可生成gif图片");
                        }
                    }
                });

                if (gifRecordTime >= gifMax) {
                    stopGif();
                }
            }
        };

        timer.schedule(gifCreateTimerTask, gifIntervel, gifIntervel);

    }

    private void stopGifTimerTask() {
        if (gifCreateTimerTask != null) {
            gifCreateTimerTask.cancel();
        }
    }

    //====================audio video change========================
    private void changeVideoPlayLayout() {
        changeAVPlayStyle(changeToVideoPlayView, changeToAudioPlayView);
        audioLayout.setVisibility(View.GONE);
        isAudio = false;
        if (currrentSubtitleSwitchFlag == 0) {
            subtitleText.setVisibility(View.VISIBLE);
        }
    }

    boolean isAudio = false;

    private void changeAudioPlayLayout() {
        changeAVPlayStyle(changeToAudioPlayView, changeToVideoPlayView);
        audioLayout.setVisibility(View.VISIBLE);
        PlayerUtil.setPortraitRequestOrientation(this);
        setLayoutVisibility(View.GONE, false);
        isAudio = true;

        playerTopLayout.setVisibility(View.VISIBLE);
        if (currrentSubtitleSwitchFlag == 0) {
            subtitleText.setVisibility(View.GONE);
        }
    }

    private void showNotification() {
        playDemoApplication.showNotification(isLocalPlay, MediaPlayActivity.class, videoId);
    }


    private void changeAVPlayStyle(TextView selectView, TextView deselectView) {
        selectView.setBackgroundResource(R.drawable.av_change_tag_bg);
        selectView.setTextColor(getResources().getColor(R.color.av_change_text_select));
        deselectView.setBackground(null);
        deselectView.setTextColor(getResources().getColor(R.color.av_change_text_normal));
    }

    private void seekToAudioBack15s() {
        if (!isPrepared) {
            return;
        }

        int currentPosition = player.getCurrentPosition();

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
        int currentPosition = player.getCurrentPosition();
        int seekToPosition = currentPosition + 15 * 1000;

        if (seekToPosition > player.getDuration()) {
            player.seekTo(player.getDuration());
        } else {
            player.seekTo(seekToPosition);
        }
    }


    //-------------------------qa view----------------------------------

    private QAView qaView;

    private boolean isQuestionTimePoint(int currentPosition) {
        if (questions == null || questions.size() < 1) {
            return false;
        }

        int questionTimePoint = questions.firstKey().intValue() * 1000; //需要换算成毫秒

        return currentPosition >= questionTimePoint;
    }

    QAView.QAViewDismissListener myQAViewDismissListener = new QAView.QAViewDismissListener() {
        @Override
        public void onDismiss(boolean isRight) {

            Question question = questions.firstEntry().getValue();

            if (question.getBackSecond() > 0 && !isRight) {
                player.seekTo(question.getBackSecond() * 1000);
            } else {
                questions.remove(questions.firstKey());
            }

            startvideoPlay();
        }
    };

    private void showQuestion() {
        if (qaView != null && qaView.isPopupWindowShown()) {
            return;
        }

        if (qaView == null) {
            qaView = new QAView(this);

            qaView.setQAViewDismissListener(myQAViewDismissListener);
        }

        qaView.setQuestion(questions.firstEntry().getValue());
        qaView.show(getWindow().getDecorView().findViewById(android.R.id.content));
    }

    private void seekTo(int currentPosition) {
        if (questions != null) {
            Iterator<Map.Entry<Integer, Question>> questionIterator = questions.entrySet().iterator();

            while (questionIterator.hasNext()) {
                Map.Entry<Integer, Question> entry = questionIterator.next();
                int qaTimePoint = entry.getKey().intValue() * 1000;

                if (qaTimePoint > currentPosition) {
                    player.seekTo(currentPosition);
                    return;
                } else {

                    Question question = entry.getValue();

                    if (question.isJump()) {
                        questionIterator.remove();
                        continue;
                    } else {
                        break;
                    }
                }
            }

            if (questions.size() > 0) {
                player.seekTo(questions.firstKey().intValue() * 1000);
                pauseVideoPlay();
                showQuestion();
            } else {
                player.seekTo(currentPosition);
            }
        } else {
            player.seekTo(currentPosition);
        }
    }
}