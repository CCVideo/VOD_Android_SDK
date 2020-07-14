package com.bokecc.vod.play;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Icon;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Rational;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bokecc.projection.ProjectionBrowseRegistryListener;
import com.bokecc.projection.ProjectionControlCallback;
import com.bokecc.projection.ProjectionControlReceiveCallback;
import com.bokecc.projection.ProjectionDLANPlayState;
import com.bokecc.projection.ProjectionDevice;
import com.bokecc.projection.ProjectionDeviceList;
import com.bokecc.projection.ProjectionDeviceListChangedListener;
import com.bokecc.projection.ProjectionDeviceManager;
import com.bokecc.projection.ProjectionIDevice;
import com.bokecc.projection.ProjectionIResponse;
import com.bokecc.projection.ProjectionIntents;
import com.bokecc.projection.ProjectionManager;
import com.bokecc.projection.ProjectionPlayControl;
import com.bokecc.projection.ProjectionPositionResponse;
import com.bokecc.projection.ProjectionUpnpService;
import com.bokecc.projection.ProjectionUtils;
import com.bokecc.projection.ProjectionVolumeResponse;
import com.bokecc.sdk.mobile.ad.DWMediaAD;
import com.bokecc.sdk.mobile.ad.DWMediaADListener;
import com.bokecc.sdk.mobile.ad.FrontADInfo;
import com.bokecc.sdk.mobile.ad.PauseADInfo;
import com.bokecc.sdk.mobile.exception.HuodeException;
import com.bokecc.sdk.mobile.play.DWIjkMediaPlayer;
import com.bokecc.sdk.mobile.play.DanmuInfo;
import com.bokecc.sdk.mobile.play.MarqueeAction;
import com.bokecc.sdk.mobile.play.MarqueeInfo;
import com.bokecc.sdk.mobile.play.MarqueeView;
import com.bokecc.sdk.mobile.play.MediaMode;
import com.bokecc.sdk.mobile.play.OnAuthMsgListener;
import com.bokecc.sdk.mobile.play.OnDanmuListListener;
import com.bokecc.sdk.mobile.play.OnDreamWinErrorListener;
import com.bokecc.sdk.mobile.play.OnExercisesMsgListener;
import com.bokecc.sdk.mobile.play.OnHotspotListener;
import com.bokecc.sdk.mobile.play.OnPlayModeListener;
import com.bokecc.sdk.mobile.play.OnQAMsgListener;
import com.bokecc.sdk.mobile.play.OnSendDanmuListener;
import com.bokecc.sdk.mobile.play.OnVisitMsgListener;
import com.bokecc.sdk.mobile.play.PlayInfo;
import com.bokecc.sdk.mobile.util.HttpUtil;
import com.bokecc.vod.ConfigUtil;
import com.bokecc.vod.HuodeApplication;
import com.bokecc.vod.R;
import com.bokecc.vod.adapter.DeviceAdapter;
import com.bokecc.vod.adapter.PlayListAdapter;
import com.bokecc.vod.data.DanmuInfoParse;
import com.bokecc.vod.data.DataSet;
import com.bokecc.vod.data.Exercise;
import com.bokecc.vod.data.HuodeVideoInfo;
import com.bokecc.vod.data.ObjectBox;
import com.bokecc.vod.data.Question;
import com.bokecc.vod.data.VideoPosition;
import com.bokecc.vod.data.VideoPositionDBHelper;
import com.bokecc.vod.data.VisitorInfo;
import com.bokecc.vod.download.DownloadController;
import com.bokecc.vod.inter.CommitOrJumpVisitorInfo;
import com.bokecc.vod.inter.ExeOperation;
import com.bokecc.vod.inter.ExercisesContinuePlay;
import com.bokecc.vod.inter.IsUseMobieNetwork;
import com.bokecc.vod.inter.MoreSettings;
import com.bokecc.vod.inter.OnDanmuSet;
import com.bokecc.vod.inter.OnEditDanmuText;
import com.bokecc.vod.inter.SelectDefinition;
import com.bokecc.vod.inter.SelectSpeed;
import com.bokecc.vod.inter.SelectVideo;
import com.bokecc.vod.utils.MultiUtils;
import com.bokecc.vod.view.CheckNetworkDialog;
import com.bokecc.vod.view.DanmuSetDialog;
import com.bokecc.vod.view.DoExerciseDialog;
import com.bokecc.vod.view.EditDanmuTextDialog;
import com.bokecc.vod.view.ExerciseGuideDialog;
import com.bokecc.vod.view.GifMakerThread;
import com.bokecc.vod.view.HotspotSeekBar;
import com.bokecc.vod.view.IsUseMobileNetworkDialog;
import com.bokecc.vod.view.LandscapeVisitorInfoDialog;
import com.bokecc.vod.view.MoreSettingsDialog;
import com.bokecc.vod.view.PortraitVisitorInfoDialog;
import com.bokecc.vod.view.ProgressObject;
import com.bokecc.vod.view.ProgressView;
import com.bokecc.vod.view.QAView;
import com.bokecc.vod.view.SelectDefinitionDialog;
import com.bokecc.vod.view.SelectSpeedDialog;
import com.bokecc.vod.view.SelectVideoDialog;
import com.bokecc.vod.view.ShowExeDialog;
import com.bokecc.vod.view.SubtitleView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.support.model.PositionInfo;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import tv.danmaku.ijk.media.player.IMediaPlayer;


public class SpeedPlayActivity extends Activity implements View.OnClickListener, TextureView.SurfaceTextureListener,
        DWIjkMediaPlayer.OnPreparedListener, DWIjkMediaPlayer.OnInfoListener, DWIjkMediaPlayer.OnBufferingUpdateListener,
        DWIjkMediaPlayer.OnCompletionListener, DWIjkMediaPlayer.OnErrorListener, OnDreamWinErrorListener, SensorEventListener {

    private String videoId, videoTitle, videoCover;
    private TextView tv_video_title, tv_current_time, tv_video_time, tv_play_speed, tv_play_definition,
            tv_video_select, tv_error_info, tv_operation, gifTips, gifCancel, tv_ad_countdown, tv_skip_ad,
            tv_know_more, tv_close_pause_ad, tv_watch_tip, tv_pre_watch_over;
    private ImageView iv_back, iv_video_full_screen, iv_next_video, iv_play_pause, iv_switch_to_audio,
            iv_more_settings, iv_create_gif, iv_save_gif, ivGifShow, ivGifStop, iv_lock_or_unlock, iv_ad_full_screen,
            iv_pause_ad;
    private ListView lv_play_list;
    private TextureView tv_video;
    private LinearLayout ll_load_video, ll_progress_and_fullscreen, ll_title_and_audio, ll_speed_def_select,
            ll_play_error, ll_audio_view, ll_confirm_or_cancel, ll_show_gif, ll_ad, ll_pre_watch_over, ll_rewatch;
    private RelativeLayout rl_play_video, rl_pause_ad;
    private Button btn_download, btn_confirm, btn_cancel;
    private HotspotSeekBar sb_progress;
    private PlayListAdapter playListAdapter;
    private DWIjkMediaPlayer player;
    private Activity activity;
    private Surface playSurface;
    private boolean isFullScreen = false, isPrepared = false, isAudioMode = false;
    private int landScapeHeight, landScapeMarginTop, videoHeight, videoWidth, sbDragProgress, playIndex,
            currentVideoSizePos = 1, currentBrightness, lastPlayPosition = 0;
    //当前播放位置、视频总时长
    private long currentPosition = 0, videoDuration = 0;
    private VideoTask videoTask;
    private AdTask adTask;
    private controlHideTask controlHideTask;
    private Timer timer, hideTimer, adTimer;
    private ArrayList<HuodeVideoInfo> videoList = new ArrayList<>();
    private List<String> videoIds;
    private float currentSpeed = 1.0f;
    // 默认设置为普清
    private int currentDefinition = DWIjkMediaPlayer.NORMAL_DEFINITION;
    //视频清晰度选项
    private Map<String, Integer> definitions;
    //切换清晰度时视频播放的位置
    private long switchDefPos = 0;
    private PlayInfo playInfo;
    //字幕
    private SubtitleView sv_subtitle;
    //在本地数据记录播放位置的辅助类
    private VideoPositionDBHelper videoPositionDBHelper;
    //记录上次播放的位置
    private VideoPosition lastVideoPosition;
    //问答数据
    private QAView qaView;
    TreeMap<Integer, Question> questions;
    //课堂练习
    private List<Exercise> exercises;
    private ShowExeDialog exeDialog;
    private DoExerciseDialog doExerciseDialog;
    private boolean isShowConfirmExerciseDialog = false;
    private int returnListenTime = 0;
    private int exerciseTimePoint;
    //视频打点数据
    private TreeMap<Integer, String> hotSpotDatas;
    //授权验证码
    private String verificationCode;
    //是否是本地播放
    private boolean isLocalPlay;
    //本地视频的路径
    private String path;
    //本地视频的格式
    private String format;
    //批量下载视频
    private List<String> batchDownload;
    //生成gif
    private boolean isGifStart = false, isGifCancel = true, isGifFinish = false;
    private GifMakerThread gifMakerThread;
    private File gifFile;
    private ProgressView gifProgressView;
    private int gifMax = 15 * 1000;
    private int gifMin = 3 * 1000;
    private int gifIntervel = 200;
    ProgressObject progressObject = new ProgressObject();
    private long lastTimeMillis;
    private int gifVideoWidth, gifVideoHeight;
    private int gifRecordTime = 0;
    private TimerTask gifCreateTimerTask;
    //是否锁定
    private boolean isLock = false;
    private int controlHide = 8;
    private String dowloadTitle;
    //0：音视频模式 1：下载视频 2：下载音频 默认下载视频
    private int downloadMode = 1;
    //广告信息获取
    private DWMediaAD dwMediaAD;
    //片头广告
    private FrontADInfo frontADInfoData;
    private String frontADClickUrl;
    //暂停广告
    private PauseADInfo pauseADInfoData;
    private String pauseAdClickUrl;
    //片头广告播放和点击信息
    private List<FrontADInfo.AdBean> frontAd;
    //片头广告数量
    private int frontAdCount = 0;
    //当前正在播放的片头广告
    private int frontAdPosition = 0;
    //是否正在播放片头广告、是否可以点击广告、是否已启动广告计时
    private boolean isPlayFrontAd = false, isCanClickAd = false, isStartAdTimer = false;
    private int skipAdTime, adTime;
    //访客信息收集
    private long showVisitorTime;
    private String visitorImageUrl, visitorJumpUrl, visitorTitle, visitorInfoId;
    private int visitorIsJump;
    private List<VisitorInfo> visitorInfos;
    private LandscapeVisitorInfoDialog visitorInfoDialog;
    private PortraitVisitorInfoDialog portraitVisitorInfoDialog;
    private boolean isShowVisitorInfoDialog = false, isVideoShowVisitorInfoDialog = false;
    //视频播放状态
    private boolean isPlayVideo = true;
    //授权验证
    private int isAllowPlayWholeVideo = 2;
    private int freeWatchTime = 0;
    private String freeWatchOverMsg = "";
    //网络状态监听
    private NetChangedReceiver netReceiver;
    //记录网络状态 0：无网络 1：WIFI 2：移动网络 3：WIFI和移动网络
    private int netWorkStatus = 1;
    private boolean isNoNetPause = false;
    private boolean isShowUseMobie = false;
    //投屏
    private String playUrl;
    private TextView tv_current_wifi, tv_projection_state;
    private List<ProjectionDevice> datas;
    private DeviceAdapter deviceAdapter;
    private ListView lv_device;
    private ImageView iv_research, iv_plus_volume, iv_minus_volume, iv_portrait_projection, iv_projection_back,
            iv_projection_screen_back;
    private Button btn_close_projection;
    private boolean isBindService = false, isProjectionContinue = true, isGetProjectionVolume = true;
    private Integer volumeValue = 0;
    private boolean isProjectioning = false, isProjectioningPause = false;
    private RelativeLayout rl_projectioning;
    private ProjectionTask projectionTask;
    private LinearLayout ll_select_projection_device, ll_searching_device, ll_not_find_device, ll_connect_projection_fail,
            ll_projection_volume, ll_projection_screen;
    private Timer projectionTimer, searchDeviceTimer;
    private SearchDeviceTask searchDeviceTask;
    private int SEARCH_DEVICE_TIME = 8;
    //监听发现投屏设备
    private ProjectionBrowseRegistryListener registryListener = new ProjectionBrowseRegistryListener();
    /**
     * 连接设备状态: 播放状态
     */
    public static final int PLAY_ACTION = 1;
    /**
     * 连接设备状态: 暂停状态
     */
    public static final int PAUSE_ACTION = 2;
    /**
     * 连接设备状态: 停止状态
     */
    public static final int STOP_ACTION = 3;
    /**
     * 投放失败
     */
    public static final int ERROR_ACTION = 4;
    //投屏控制
    private ProjectionPlayControl projectionPlayControl = new ProjectionPlayControl();
    private Handler mHandler = new ProjectionHandler();
    private BroadcastReceiver mTransportStateBroadcastReceiver;

    //重力感应旋转方向
    private int mX, mY, mZ;
    private long lastSensorTime = 0;
    private SensorManager sensorManager;

    //滑动调节进度亮度和音量
    private float downX, downY, upX, upY, xMove, yMove, absxMove, absyMove, lastX, lastY;
    private AudioManager audioManager;
    private int currentVolume, maxVolume;
    private int maxBrightness = 100, halfWidth = 0, controlChange = 70;
    private boolean isChangeBrightness = false;
    private LinearLayout ll_volume;
    private ProgressBar pb_volume;
    private LinearLayout ll_brightness;
    private ProgressBar pb_brightness;
    private long slideProgress;
    private TextView tv_slide_progress;

    //首次加载失败启用备用线路
    private boolean isBackupPlay = false;
    private boolean isFirstBuffer = true;

    //跑马灯
    private MarqueeView mv_video;

    private final int SMALL_WINDOW_PLAY_OR_PAUSE = 5;
    private ImageView iv_small_window_play;
    private SmallWindowReceiver smallWindowReceiver;
    private final String smallWindowAction = "com.bokecc.vod.play.SMALL_WINDOW";
    private ArrayList<RemoteAction> actions;
    private RemoteAction pauseRemoteAction, playRemoteAction;
    private boolean isSmallWindow = false;
    private PictureInPictureParams.Builder builder;

    private ImageView iv_landscape_screenshot, iv_portrait_screenshot;

    //网速
    private String netSpeed;
    private TextView tv_loading;
    private Timer netSpeedTimer;
    private NetSpeedTask netSpeedTask;

    private IDanmakuView dm_view;
    private DanmakuContext danmakuContext;
    private DanmuInfoParse danmuInfoParse;
    private HotspotSeekBar sb_portrait_progress;
    private TextView tv_portrait_current_time, tv_portrait_video_time;
    private LinearLayout ll_landscape_progress, ll_portrait_progress, ll_landscape_danmu,
            ll_portrait_danmu_off, ll_portrait_danmu_on, ll_landscape_danmu_set_send;
    private TextView tv_input_danmu, tv_portrait_input_danmu;
    private int danmuSec = -1, currentMinutePos = 0;
    private int getDanmuInterval = 60 * 1000;
    private ImageView iv_landscape_danmu_set, iv_portrait_danmu_on, iv_portrait_danmu_set,
            iv_landscape_danmu_switch;
    private int currentOpaqueness = 100, currentFontSizeLevel = 2, currentDanmuSpeedLevel = 2,
            currentDisplayArea = 100;
    private float fontSizeScale = 1.5f, danmuSpeed = 1.0f;
    private boolean isDanmuOn = true, isCanSendDanmu = true, isPlayAfterSendDanmu = true;
    private RelativeLayout rl_danmu;
    private int sendDanmuInterval = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_play);
        MultiUtils.setStatusBarColor(this, R.color.black, false);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        activity = this;
        regNetworkReceiver();
        initView();
        initPlayer();
    }

    //注册网络状态监听
    private void regNetworkReceiver() {
        netWorkStatus = MultiUtils.getNetWorkStatus(activity);
        if (netReceiver == null) {
            netReceiver = new NetChangedReceiver();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netReceiver, filter);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        videoId = getIntent().getStringExtra("videoId");
        videoTitle = getIntent().getStringExtra("videoTitle");
        videoCover = getIntent().getStringExtra("videoCover");
        format = getIntent().getStringExtra("format");
        isLocalPlay = getIntent().getBooleanExtra("isLocalPlay", false);


        iv_back = findViewById(R.id.iv_back);
        iv_video_full_screen = findViewById(R.id.iv_video_full_screen);
        iv_next_video = findViewById(R.id.iv_next_video);
        iv_play_pause = findViewById(R.id.iv_play_pause);
        iv_switch_to_audio = findViewById(R.id.iv_switch_to_audio);
        iv_more_settings = findViewById(R.id.iv_more_settings);
        iv_create_gif = findViewById(R.id.iv_create_gif);
        iv_save_gif = findViewById(R.id.iv_save_gif);
        ivGifShow = findViewById(R.id.gif_show);
        ivGifStop = findViewById(R.id.iv_gif_stop);
        iv_lock_or_unlock = findViewById(R.id.iv_lock_or_unlock);
        iv_ad_full_screen = findViewById(R.id.iv_ad_full_screen);
        tv_video_title = findViewById(R.id.tv_video_title);
        tv_current_time = findViewById(R.id.tv_current_time);
        tv_video_time = findViewById(R.id.tv_video_time);
        tv_play_speed = findViewById(R.id.tv_play_speed);
        tv_play_definition = findViewById(R.id.tv_play_definition);
        tv_video_select = findViewById(R.id.tv_video_select);
        tv_error_info = findViewById(R.id.tv_error_info);
        tv_operation = findViewById(R.id.tv_operation);
        gifTips = findViewById(R.id.gif_tips);
        gifCancel = findViewById(R.id.gif_cancel);
        tv_ad_countdown = findViewById(R.id.tv_ad_countdown);
        tv_skip_ad = findViewById(R.id.tv_skip_ad);
        tv_know_more = findViewById(R.id.tv_know_more);
        tv_close_pause_ad = findViewById(R.id.tv_close_pause_ad);
        tv_watch_tip = findViewById(R.id.tv_watch_tip);
        tv_pre_watch_over = findViewById(R.id.tv_pre_watch_over);
        lv_play_list = findViewById(R.id.lv_play_list);
        tv_video = findViewById(R.id.tv_video);
        ll_load_video = findViewById(R.id.ll_load_video);
        ll_progress_and_fullscreen = findViewById(R.id.ll_progress_and_fullscreen);
        ll_select_projection_device = findViewById(R.id.ll_select_projection_device);
        ll_projection_screen = findViewById(R.id.ll_projection_screen);
        ll_projection_volume = findViewById(R.id.ll_projection_volume);
        ll_searching_device = findViewById(R.id.ll_searching_device);
        ll_not_find_device = findViewById(R.id.ll_not_find_device);
        ll_connect_projection_fail = findViewById(R.id.ll_connect_projection_fail);
        ll_title_and_audio = findViewById(R.id.ll_title_and_audio);
        ll_speed_def_select = findViewById(R.id.ll_speed_def_select);
        ll_play_error = findViewById(R.id.ll_play_error);
        ll_audio_view = findViewById(R.id.ll_audio_view);
        rl_play_video = findViewById(R.id.rl_play_video);
        rl_pause_ad = findViewById(R.id.rl_pause_ad);
        sb_progress = findViewById(R.id.sb_progress);
        sv_subtitle = findViewById(R.id.sv_subtitle);
        btn_download = findViewById(R.id.btn_download);
        btn_confirm = findViewById(R.id.btn_confirm);
        btn_cancel = findViewById(R.id.btn_cancel);
        ll_confirm_or_cancel = findViewById(R.id.ll_confirm_or_cancel);
        ll_show_gif = findViewById(R.id.ll_show_gif);
        ll_pre_watch_over = findViewById(R.id.ll_pre_watch_over);
        ll_rewatch = findViewById(R.id.ll_rewatch);
        ll_ad = findViewById(R.id.ll_ad);
        iv_pause_ad = findViewById(R.id.iv_pause_ad);
        iv_projection_back = findViewById(R.id.iv_projection_back);
        iv_projection_screen_back = findViewById(R.id.iv_projection_screen_back);
        gifProgressView = findViewById(R.id.gif_progress_view);
        gifProgressView.setMaxDuration(gifMax);
        gifProgressView.setMinTime(gifMin);
        gifProgressView.setData(progressObject);
        tv_current_wifi = findViewById(R.id.tv_current_wifi);
        tv_projection_state = findViewById(R.id.tv_projection_state);
        lv_device = findViewById(R.id.lv_device);
        iv_research = findViewById(R.id.iv_research);
        iv_plus_volume = findViewById(R.id.iv_plus_volume);
        iv_minus_volume = findViewById(R.id.iv_minus_volume);
        iv_portrait_projection = findViewById(R.id.iv_portrait_projection);
        rl_projectioning = findViewById(R.id.rl_projectioning);
        btn_close_projection = findViewById(R.id.btn_close_projection);

        ll_volume = findViewById(R.id.ll_volume);
        ll_brightness = findViewById(R.id.ll_brightness);
        pb_volume = findViewById(R.id.pb_volume);
        pb_brightness = findViewById(R.id.pb_brightness);
        tv_slide_progress = findViewById(R.id.tv_slide_progress);

        mv_video = findViewById(R.id.mv_video);
        iv_small_window_play = findViewById(R.id.iv_small_window_play);

        iv_landscape_screenshot = findViewById(R.id.iv_landscape_screenshot);
        iv_portrait_screenshot = findViewById(R.id.iv_portrait_screenshot);
        tv_loading = findViewById(R.id.tv_loading);

        dm_view = findViewById(R.id.dm_view);
        sb_portrait_progress = findViewById(R.id.sb_portrait_progress);
        tv_portrait_current_time = findViewById(R.id.tv_portrait_current_time);
        tv_portrait_video_time = findViewById(R.id.tv_portrait_video_time);
        ll_landscape_progress = findViewById(R.id.ll_landscape_progress);
        ll_portrait_progress = findViewById(R.id.ll_portrait_progress);
        ll_landscape_danmu = findViewById(R.id.ll_landscape_danmu);
        tv_input_danmu = findViewById(R.id.tv_input_danmu);
        tv_input_danmu.setOnClickListener(this);
        iv_landscape_danmu_set = findViewById(R.id.iv_landscape_danmu_set);
        iv_landscape_danmu_set.setOnClickListener(this);
        ll_portrait_danmu_off = findViewById(R.id.ll_portrait_danmu_off);
        ll_portrait_danmu_off.setOnClickListener(this);
        ll_portrait_danmu_on = findViewById(R.id.ll_portrait_danmu_on);
        iv_portrait_danmu_on = findViewById(R.id.iv_portrait_danmu_on);
        iv_portrait_danmu_on.setOnClickListener(this);
        tv_portrait_input_danmu = findViewById(R.id.tv_portrait_input_danmu);
        tv_portrait_input_danmu.setOnClickListener(this);
        iv_portrait_danmu_set = findViewById(R.id.iv_portrait_danmu_set);
        iv_portrait_danmu_set.setOnClickListener(this);
        iv_landscape_danmu_switch = findViewById(R.id.iv_landscape_danmu_switch);
        iv_landscape_danmu_switch.setOnClickListener(this);
        ll_landscape_danmu_set_send = findViewById(R.id.ll_landscape_danmu_set_send);
        rl_danmu = findViewById(R.id.rl_danmu);

        tv_video_title.setText(videoTitle);
        iv_back.setOnClickListener(this);
        iv_video_full_screen.setOnClickListener(this);
        iv_next_video.setOnClickListener(this);
        iv_play_pause.setOnClickListener(this);
        tv_play_speed.setOnClickListener(this);
        tv_play_definition.setOnClickListener(this);
        tv_video_select.setOnClickListener(this);
        iv_switch_to_audio.setOnClickListener(this);
        iv_more_settings.setOnClickListener(this);
        btn_download.setOnClickListener(this);
        btn_confirm.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
        iv_create_gif.setOnClickListener(this);
        iv_save_gif.setOnClickListener(this);
        gifCancel.setOnClickListener(this);
        ivGifStop.setOnClickListener(this);
        tv_skip_ad.setOnClickListener(this);
        tv_know_more.setOnClickListener(this);
        iv_research.setOnClickListener(this);
        iv_lock_or_unlock.setOnClickListener(this);
        iv_ad_full_screen.setOnClickListener(this);
        iv_pause_ad.setOnClickListener(this);
        tv_close_pause_ad.setOnClickListener(this);
        ll_rewatch.setOnClickListener(this);
        iv_small_window_play.setOnClickListener(this);
        iv_projection_back.setOnClickListener(this);
        iv_plus_volume.setOnClickListener(this);
        iv_minus_volume.setOnClickListener(this);
        iv_portrait_projection.setOnClickListener(this);
        btn_close_projection.setOnClickListener(this);
        iv_projection_screen_back.setOnClickListener(this);
        tv_video.setSurfaceTextureListener(this);
        iv_landscape_screenshot.setOnClickListener(this);
        iv_portrait_screenshot.setOnClickListener(this);
        //本地播放音频
        if (!TextUtils.isEmpty(format) && format.equals(".mp3")) {
            isAudioMode = true;
            ll_audio_view.setVisibility(View.VISIBLE);
        }

        batchDownload = new ArrayList<>();
        videoList = getIntent().getParcelableArrayListExtra("videoDatas");
        if (videoList != null && videoList.size() > 0) {
            videoIds = new ArrayList<>();
            for (int i = 0; i < videoList.size(); i++) {
                HuodeVideoInfo videoInfo = videoList.get(i);
                if (videoInfo != null) {
                    videoIds.add(videoList.get(i).getVideoId());
                }
            }
            playIndex = videoIds.indexOf(videoId);
            playListAdapter = new PlayListAdapter(SpeedPlayActivity.this, videoList);
            lv_play_list.setAdapter(playListAdapter);
        }

        //拖动视频
        sb_progress.setOnSeekBarChangeListener(new HotspotSeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(HotspotSeekBar seekBar) {
                returnListenTime = seekBar.getProgress();
            }

            @Override
            public void onStopTrackingTouch(HotspotSeekBar seekBar, float trackStopPercent) {
                int stopPostion = (int) (trackStopPercent * player.getDuration());
                if (isProjectioning) {
                    projectionPlayControl.seek(stopPostion, new ProjectionControlCallback() {
                        @Override
                        public void success(ProjectionIResponse response) {

                        }

                        @Override
                        public void fail(ProjectionIResponse response) {

                        }
                    });
                } else {
                    player.seekTo(stopPostion);
                    seekToDanmu(stopPostion);
                    danmuSec = (stopPostion / getDanmuInterval) - 1;
                    //拖动进度条展示课堂练习
                    if (isShowExercise(stopPostion)) {
                        isShowConfirmExerciseDialog = true;
                    } else {
                        isShowConfirmExerciseDialog = false;
                    }
                }

            }
        });

        sb_portrait_progress.setOnSeekBarChangeListener(new HotspotSeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(HotspotSeekBar seekBar) {
                returnListenTime = seekBar.getProgress();
            }

            @Override
            public void onStopTrackingTouch(HotspotSeekBar seekBar, float trackStopPercent) {
                int stopPostion = (int) (trackStopPercent * player.getDuration());
                if (isProjectioning) {
                    projectionPlayControl.seek(stopPostion, new ProjectionControlCallback() {
                        @Override
                        public void success(ProjectionIResponse response) {

                        }

                        @Override
                        public void fail(ProjectionIResponse response) {

                        }
                    });
                } else {
                    player.seekTo(stopPostion);
                    seekToDanmu(stopPostion);
                    danmuSec = (stopPostion / getDanmuInterval) - 1;
                    //拖动进度条展示课堂练习
                    if (isShowExercise(stopPostion)) {
                        isShowConfirmExerciseDialog = true;
                    } else {
                        isShowConfirmExerciseDialog = false;
                    }
                }
            }
        });

        //点击打点位置，从这个位置开始播放
        sb_progress.setOnIndicatorTouchListener(new HotspotSeekBar.OnIndicatorTouchListener() {
            @Override
            public void onIndicatorTouch(int currentPosition) {
                player.seekTo(currentPosition * 1000);
                seekToDanmu(currentPosition * 1000);
            }
        });

        sb_portrait_progress.setOnIndicatorTouchListener(new HotspotSeekBar.OnIndicatorTouchListener() {
            @Override
            public void onIndicatorTouch(int currentPosition) {
                player.seekTo(currentPosition * 1000);
                seekToDanmu(currentPosition * 1000);
            }
        });

        lv_play_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isProjectioning) {
                    MultiUtils.showToast(activity, "投屏中，暂不支持切换");
                    return;
                }
                HuodeVideoInfo item = (HuodeVideoInfo) playListAdapter.getItem(position);
                if (item.isShowSelectButton()) {
                    if (item.isSelectedDownload()) {
                        item.setSelectedDownload(false);
                    } else {
                        item.setSelectedDownload(true);
                    }
                    playListAdapter.notifyDataSetChanged();
                } else {
                    videoId = item.getVideoId();
                    videoTitle = item.getVideoTitle();
                    videoCover = item.getVideoCover();
                    playIndex = position;
                    resetInfo();
                    playVideoOrAudio(isAudioMode, true);
                    player.resetPlayedAndPausedTime();
                }

            }
        });

        rl_play_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ll_pre_watch_over.getVisibility() == View.VISIBLE) {
                    return;
                }
                if (isPlayFrontAd) {
                    knowMoreFrontAdInfo();
                    return;
                }
                if (!isPrepared) {
                    return;
                }
                if (iv_back.getVisibility() == View.VISIBLE) {
                    hideViews();
                    return;
                }
                if (isProjectioning) {
                    iv_lock_or_unlock.setVisibility(View.GONE);
                    iv_portrait_screenshot.setVisibility(View.GONE);
                    iv_create_gif.setVisibility(View.GONE);
                    iv_landscape_screenshot.setVisibility(View.GONE);
                } else {
                    if (isFullScreen) {
                        iv_lock_or_unlock.setVisibility(View.VISIBLE);
                        iv_create_gif.setVisibility(View.VISIBLE);
                        iv_landscape_screenshot.setVisibility(View.VISIBLE);
                    }
                }
                if (isLock) {
                    iv_lock_or_unlock.setImageResource(R.mipmap.iv_lock);
                    iv_lock_or_unlock.setVisibility(View.VISIBLE);
                } else {
                    iv_lock_or_unlock.setImageResource(R.mipmap.iv_unlock);
                    showViews();
                }
            }
        });

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        pb_volume.setMax(maxVolume);
        pb_volume.setProgress(currentVolume);

        //获取当前亮度
        currentBrightness = MultiUtils.getSystemBrightness(activity);
        pb_brightness.setMax(maxBrightness);
        pb_brightness.setProgress(currentBrightness);

        //滑动调节
        rl_play_video.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getX();
                        downY = event.getY();
                        lastX = downX;
                        lastY = downY;
                        slideProgress = currentPosition;
                        halfWidth = MultiUtils.getScreenWidth(activity) / 2;
                        if (downX > halfWidth) {
                            isChangeBrightness = false;
                            controlChange = 70;
                        } else {
                            isChangeBrightness = true;
                            controlChange = 15;
                        }

                        break;

                    case MotionEvent.ACTION_MOVE:
                        float x = event.getX();
                        float y = event.getY();

                        float xMoveVolume = x - lastX;
                        float yMoveVolume = y - lastY;
                        float absxMoveVolume = Math.abs(xMoveVolume);
                        float absyMoveVolume = Math.abs(yMoveVolume);

                        if (absyMoveVolume > absxMoveVolume && absyMoveVolume > controlChange && !isLock) {
                            lastX = x;
                            lastY = y;
                            if (isChangeBrightness) {
                                //调节亮度
                                int changeBrightness = (int) (absyMoveVolume / controlChange);
                                if (yMoveVolume > 0) {
                                    currentBrightness = currentBrightness - changeBrightness;
                                } else {
                                    currentBrightness = currentBrightness + changeBrightness;
                                }
                                if (currentBrightness < 0) {
                                    currentBrightness = 0;
                                }

                                if (currentBrightness > maxBrightness) {
                                    currentBrightness = maxBrightness;
                                }
                                ll_brightness.setVisibility(View.VISIBLE);
                                ll_volume.setVisibility(View.GONE);
                                changeBrightness(activity, currentBrightness);
                                pb_brightness.setProgress(currentBrightness);
                            } else {
                                currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                //调节音量
                                int changeVolume = (int) (absyMoveVolume / controlChange);
                                if (yMoveVolume > 0) {
                                    currentVolume = currentVolume - changeVolume;
                                } else {
                                    currentVolume = currentVolume + changeVolume;
                                }
                                if (currentVolume < 0) {
                                    currentVolume = 0;
                                }

                                if (currentVolume > maxVolume) {
                                    currentVolume = maxVolume;
                                }
                                ll_volume.setVisibility(View.VISIBLE);
                                ll_brightness.setVisibility(View.GONE);
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
                                pb_volume.setProgress(currentVolume);
                            }

                        } else if (absxMoveVolume > absyMoveVolume && absxMoveVolume > 50 && !isLock) {
                            lastX = x;
                            lastY = y;
                            int screenWidth = MultiUtils.getScreenWidth(activity);
                            long changeProgress = (long) (absxMoveVolume * videoDuration / screenWidth);

                            if (xMoveVolume > 0) {
                                //往右滑
                                slideProgress = slideProgress + changeProgress;
                            } else {
                                //往左滑
                                slideProgress = slideProgress - changeProgress;
                            }
                            if (slideProgress > videoDuration) {
                                slideProgress = videoDuration;
                            }
                            if (slideProgress < 0) {
                                slideProgress = 0;
                            }
                            String videoTime = MultiUtils.millsecondsToMinuteSecondStr(videoDuration);
                            String currentTime = MultiUtils.millsecondsToMinuteSecondStr(slideProgress);
                            tv_slide_progress.setVisibility(View.VISIBLE);
                            tv_slide_progress.setText(currentTime + "/" + videoTime);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        upX = event.getX();
                        upY = event.getY();

                        xMove = upX - downX;
                        yMove = upY - downY;
                        absxMove = Math.abs(xMove);
                        absyMove = Math.abs(yMove);

                        if (absxMove >= absyMove && absxMove > 50 && !isLock) {
                            //调节进度
                            tv_slide_progress.setVisibility(View.GONE);
                            player.seekTo((int) slideProgress);
                            seekToDanmu((int) slideProgress);
                        }
                        break;
                }
                return false;
            }
        });

        verificationCode = MultiUtils.getVerificationCode();
        //获取上次播放的位置
        videoPositionDBHelper = new VideoPositionDBHelper(ObjectBox.get());
        getLastVideoPostion();

        lv_device.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ProjectionDevice item = (ProjectionDevice) deviceAdapter.getItem(position);
                if (ProjectionUtils.isNull(item)) {
                    return;
                }

                ProjectionManager.getInstance().setSelectedDevice(item);
                Device device = item.getDevice();
                if (ProjectionUtils.isNull(device)) {
                    return;
                }
                ll_select_projection_device.setVisibility(View.GONE);
                cancelSearchDeviceTimer();
                rl_projectioning.setVisibility(View.VISIBLE);
                MultiUtils.setStatusBarColor(activity, R.color.black, false);
                playProjection();
            }
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
        }

        //小窗播放
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            iv_small_window_play.setVisibility(View.VISIBLE);
            actions = new ArrayList<>();
            Icon icon = Icon.createWithResource(activity, R.mipmap.iv_small_window_pause);
            PendingIntent pauseIntent = PendingIntent.getBroadcast(activity,
                    SMALL_WINDOW_PLAY_OR_PAUSE,
                    new Intent(smallWindowAction).putExtra("control", 1),
                    PendingIntent.FLAG_UPDATE_CURRENT);

            pauseRemoteAction = new RemoteAction(icon, "", "", pauseIntent);


            Icon iconPlay = Icon.createWithResource(activity, R.mipmap.iv_small_window_play);
            PendingIntent playIntent = PendingIntent.getBroadcast(activity,
                    SMALL_WINDOW_PLAY_OR_PAUSE,
                    new Intent(smallWindowAction).putExtra("control", 2),
                    PendingIntent.FLAG_UPDATE_CURRENT);
            playRemoteAction = new RemoteAction(iconPlay, "", "", playIntent);

            smallWindowReceiver = new SmallWindowReceiver();
            IntentFilter intentFilter = new IntentFilter(smallWindowAction);
            registerReceiver(smallWindowReceiver, intentFilter);
        }

        //弹幕
        danmakuContext = DanmakuContext.create();
        HashMap<Integer, Boolean> overlappingEnable = new HashMap<Integer, Boolean>();
        overlappingEnable.put(BaseDanmaku.TYPE_SCROLL_RL, true);
        overlappingEnable.put(BaseDanmaku.TYPE_FIX_TOP, true);
        danmakuContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 2)
                .setDuplicateMergingEnabled(false)
                .preventOverlapping(overlappingEnable)
                .setDanmakuMargin(50);
        if (dm_view != null) {
            danmuInfoParse = new DanmuInfoParse();
            dm_view.setCallback(new master.flame.danmaku.controller.DrawHandler.Callback() {
                @Override
                public void updateTimer(DanmakuTimer timer) {

                }

                @Override
                public void drawingFinished() {

                }

                @Override
                public void danmakuShown(BaseDanmaku danmaku) {

                }

                @Override
                public void prepared() {
                    dm_view.start();
                    if (!isDanmuOn) {
                        dm_view.hide();
                    }
                }
            });
            dm_view.prepare(danmuInfoParse, danmakuContext);
            dm_view.enableDanmakuDrawingCache(true);
        }

    }

    /**
     * brightnessValue 取值范围0-100
     */
    private void changeBrightness(Activity context, int brightnessValue) {
        Window localWindow = context.getWindow();
        WindowManager.LayoutParams localLayoutParams = localWindow.getAttributes();
        localLayoutParams.screenBrightness = brightnessValue / 100.0F;
        localWindow.setAttributes(localLayoutParams);
    }

    private void getLastVideoPostion() {
        lastVideoPosition = videoPositionDBHelper.getVideoPosition(videoId);
        if (lastVideoPosition == null) {
            lastPlayPosition = 0;
            if (TextUtils.isEmpty(videoId)) {
                return;
            }
            lastVideoPosition = new VideoPosition(videoId, 0);

        } else {
            lastPlayPosition = lastVideoPosition.getPosition();
        }
    }

    //初始化播放器
    private void initPlayer() {
        player = new DWIjkMediaPlayer();
        player.setOnPreparedListener(this);
        player.setOnInfoListener(this);
        player.setOnBufferingUpdateListener(this);
        player.setOnCompletionListener(this);
        player.setOnDreamWinErrorListener(this);
        player.setOnErrorListener(this);
        //开启防录屏，会使加密视频投屏功能不能正常使用
//        player.setAntiRecordScreen(this);
        //设置CustomId
        player.setCustomId("HIHA2019");
        //获取字幕信息
        sv_subtitle.getSubtitlesInfo(player);
        //获得问答信息
        player.setOnQaMsgListener(new OnQAMsgListener() {
            @Override
            public void onQAMessage(JSONArray jsonArray) {
                if (questions == null) {
                    questions = new TreeMap<>();
                    createQuestionMap(jsonArray);
                }
            }
        });
        player.setOnSeekCompleteListener(new IMediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(IMediaPlayer iMediaPlayer) {
                if (qaView != null && qaView.isPopupWindowShown()) {
                    player.pauseWithoutAnalyse();
                }
            }
        });

        //设置访客信息收集监听器
        visitorInfos = new ArrayList<>();
        player.setOnVisitMsgListener(new OnVisitMsgListener() {
            @Override
            public void onVisitMsg(int appearTime, String imageURL, int isJump, String jumpURL, String title, String visitorId, JSONArray visitorMessage) {
                showVisitorTime = appearTime * 1000;
                visitorImageUrl = imageURL;
                visitorIsJump = isJump;
                visitorJumpUrl = jumpURL;
                visitorTitle = title;
                visitorInfoId = visitorId;

                if (visitorMessage != null && visitorMessage.length() > 0) {
                    isShowVisitorInfoDialog = true;
                    for (int i = 0; i < visitorMessage.length(); i++) {
                        try {
                            VisitorInfo visitorInfo = new VisitorInfo(visitorMessage.getJSONObject(i));
                            visitorInfos.add(visitorInfo);
                        } catch (JSONException e) {

                        }
                    }

                }
            }
        });

        //课堂练习
        exercises = new ArrayList<>();
        player.setOnExercisesMsgListener(new OnExercisesMsgListener() {
            @Override
            public void onExercisesMessage(JSONArray exArray) {
                if (exArray != null && exArray.length() > 0) {
                    for (int i = 0; i < exArray.length(); i++) {
                        try {
                            Exercise exercise = new Exercise(exArray.getJSONObject(i));
                            exercises.add(exercise);
                        } catch (JSONException e) {

                        }
                    }
                }
            }
        });

        //设置鉴权监听器
        player.setOnAuthMsgListener(new OnAuthMsgListener() {
            @Override
            public void onAuthMsg(final int enable, final int freetime, final String messaage, final MarqueeInfo marqueeInfo) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isAllowPlayWholeVideo = enable;
                        freeWatchTime = freetime;
                        freeWatchOverMsg = messaage;
                        if (isAllowPlayWholeVideo == 0) {
                            if (freeWatchTime > 0) {
                                tv_watch_tip.setVisibility(View.VISIBLE);
                            }
                            int minute = freeWatchTime / 60;
                            int second = freeWatchTime % 60;
                            tv_watch_tip.setText("可试看" + minute + "分钟" + second + "秒，购买会员查看完整版");
                        }
                        tv_pre_watch_over.setText(freeWatchOverMsg);

                        //设置跑马灯
                        if (marqueeInfo != null) {
                            String type = marqueeInfo.getType();
                            int loop = marqueeInfo.getLoop();
                            ArrayList<MarqueeAction> action = marqueeInfo.getAction();
                            mv_video.setLoop(loop);
                            mv_video.setMarqueeActions(action);
                            MarqueeInfo.TextBean textBean = marqueeInfo.getTextBean();
                            MarqueeInfo.ImageBean imageBean = marqueeInfo.getImageBean();
                            if (!TextUtils.isEmpty(type) && type.equals("text")) {
                                mv_video.setType(MarqueeView.TEXT);
                                if (textBean != null) {
                                    String content = textBean.getContent();
                                    if (!TextUtils.isEmpty(content)) {
                                        mv_video.setTextContent(content);
                                    }
                                    int font_size = textBean.getFont_size();
                                    mv_video.setTextFontSize(font_size);
                                    String color = textBean.getColor();
                                    if (!TextUtils.isEmpty(color) && color.length() == 8) {
                                        String textColor = "#" + color.substring(2, 8);
                                        if (textColor.length() == 7) {
                                            //textColor：文字颜色，格式如：#ffffff
                                            mv_video.setTextColor(textColor);
                                        }
                                    }

                                }
                            } else if (!TextUtils.isEmpty(type) && type.equals("image")) {
                                mv_video.setType(MarqueeView.IMAGE);
                                if (imageBean != null) {
                                    mv_video.setMarqueeImage(activity, imageBean.getImage_url(), imageBean.getWidth(), imageBean.getHeight());
                                }
                            }
                        }
                    }
                });
            }
        });

        //获得视频打点信息
        player.setOnHotspotListener(new OnHotspotListener() {
            @Override
            public void onHotspots(TreeMap<Integer, String> hotspotMap) {
                hotSpotDatas = hotspotMap;
            }
        });
        // DRM加密播放
        player.setDRMServerPort(HuodeApplication.getDrmServerPort());
        if (isLocalPlay) {
            btn_download.setVisibility(View.INVISIBLE);
            iv_switch_to_audio.setVisibility(View.GONE);
            iv_portrait_projection.setVisibility(View.GONE);
            //离线播放
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                path = Environment.getExternalStorageDirectory() + "/".concat(ConfigUtil.DOWNLOAD_PATH).concat("/").concat(videoTitle).concat(format);

                if (!new File(path).exists()) {

                    return;
                }
            }
            try {
                player.setOfflineVideoPath(path, activity);
                hideOnlineOperation();
                HuodeApplication.getDRMServer().resetLocalPlay();
                player.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //获取广告信息
            getAdInfo();
        }

        //查询弹幕结果
        player.setOnDanmuListListener(new OnDanmuListListener() {
            @Override
            public void onSuccess(final ArrayList<DanmuInfo> danmuInfos) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dm_view.removeAllDanmakus(true);
                        for (DanmuInfo danmuInfo : danmuInfos) {
                            addDanmu(danmuInfo.getContent(), danmuInfo.getFc(), danmuInfo.getPt(), false);
                        }
                        seekToDanmu(currentPosition);
                    }
                });
            }

            @Override
            public void onFail(String msg) {
                danmuSec = currentMinutePos - 1;
            }
        });

    }

    //获得广告信息
    private void getAdInfo() {

        if (netWorkStatus == 0) {
            tv_error_info.setText("请检查你的网络连接");
            showPlayErrorView();
            hideOtherOperations();
            tv_operation.setText("重试");
            tv_operation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hidePlayErrorView();
                    getAdInfo();
                }
            });
            return;
        }

        if (netWorkStatus == 2) {
            IsUseMobileNetworkDialog isUseMobileNetworkDialog = new IsUseMobileNetworkDialog(activity, new IsUseMobieNetwork() {
                @Override
                public void exit() {
                    finish();
                    isShowUseMobie = false;
                }

                @Override
                public void continuePlay() {
                    requestAd();
                    isShowUseMobie = false;
                }
            });
            if (!isUseMobileNetworkDialog.isShowing()) {
                isUseMobileNetworkDialog.show();
                isShowUseMobie = true;
            }
            return;
        }
        requestAd();
    }

    private void requestAd() {
        startNetSpeedTimer();

        isPrepared = false;
        frontAdPosition = 0;
        dwMediaAD = new DWMediaAD(dwMediaADListener, ConfigUtil.USERID, videoId);
        dwMediaAD.getFrontAD();
        dwMediaAD.getPauseAD();
    }

    private DWMediaADListener dwMediaADListener = new DWMediaADListener() {

        @Override
        public void onFrontAD(FrontADInfo frontADInfo) {
            frontADInfoData = frontADInfo;
            if (frontADInfo != null && frontADInfo.getAd() != null) {
                frontAd = frontADInfo.getAd();
                frontAdCount = frontAd.size();
                FrontADInfo.AdBean adBean = frontAd.get(frontAdPosition);
                if (adBean != null) {
                    playFrontAd(adBean);
                }
            }

        }

        @Override
        public void onPauseAD(PauseADInfo pauseADInfo) {
            pauseADInfoData = pauseADInfo;
        }

        @Override
        public void onFrontADError(HuodeException e) {
            //播放正片
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    playVideoOrAudio(isAudioMode, true);
                }
            });
        }

        @Override
        public void onPauseADError(HuodeException e) {

        }
    };

    //播放片头广告
    private void playFrontAd(final FrontADInfo.AdBean adBean) {
        try {
            String material = adBean.getMaterial();
            if (!TextUtils.isEmpty(material) && (material.endsWith(".mp4") || material.endsWith(".pcm"))) {
                frontADClickUrl = adBean.getClickurl();
                isPlayFrontAd = true;
                player.pause();
                player.stop();
                player.reset();
                player.setVideoPlayInfo(null, null, null, null, activity);
                player.setDataSource(material);
                player.setSurface(playSurface);
                HuodeApplication.getDRMServer().reset();
                player.prepareAsync();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideViews();
                        if (frontADInfoData != null) {
                            if (frontADInfoData.getCanskip() == 1) {
                                tv_skip_ad.setVisibility(View.VISIBLE);
                                skipAdTime = frontADInfoData.getSkipTime();
                            } else {
                                tv_skip_ad.setVisibility(View.GONE);
                            }
                            adTime = frontADInfoData.getTime();
                            if (frontADInfoData.getCanclick() == 1) {
                                tv_know_more.setVisibility(View.VISIBLE);
                            } else {
                                tv_know_more.setVisibility(View.GONE);
                            }
                        }

                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        playVideoOrAudio(isAudioMode, true);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //展示访客信息框
    private void showVisitorInfoDialog() {
        if (isVideoShowVisitorInfoDialog) {
            return;
        }
        if (isShowVisitorDialog((int) currentPosition) && visitorInfos != null && visitorInfos.size() > 0) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (visitorInfoDialog != null && visitorInfoDialog.isShowing()) {
                    return;
                }
                if (portraitVisitorInfoDialog != null && portraitVisitorInfoDialog.isShowing()) {
                    return;
                }
                if (!isShowVisitorInfoDialog) {
                    return;
                }
                visitorInfoDialog = new LandscapeVisitorInfoDialog(activity, videoId, visitorImageUrl, visitorJumpUrl,
                        visitorTitle, visitorInfoId, visitorIsJump, visitorInfos, new CommitOrJumpVisitorInfo() {
                    @Override
                    public void commit() {
                        isShowVisitorInfoDialog = false;
                        if (!isPlayVideo) {
                            playOrPauseVideo();
                        }
                    }

                    @Override
                    public void jump() {
                        isShowVisitorInfoDialog = false;
                        if (!isPlayVideo) {
                            playOrPauseVideo();
                        }
                    }
                });
                visitorInfoDialog.setCanceledOnTouchOutside(false);
                isVideoShowVisitorInfoDialog = true;
                visitorInfoDialog.show();
                visitorInfoDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                });
                playOrPauseVideo();

            } else {
                if (portraitVisitorInfoDialog != null && portraitVisitorInfoDialog.isShowing()) {
                    return;
                }
                if (visitorInfoDialog != null && visitorInfoDialog.isShowing()) {
                    return;
                }
                if (!isShowVisitorInfoDialog) {
                    return;
                }
                portraitVisitorInfoDialog = new PortraitVisitorInfoDialog(activity, videoId, visitorImageUrl, visitorJumpUrl,
                        visitorTitle, visitorInfoId, visitorIsJump, visitorInfos, new CommitOrJumpVisitorInfo() {
                    @Override
                    public void commit() {
                        isShowVisitorInfoDialog = false;
                        if (!isPlayVideo) {
                            playOrPauseVideo();
                        }
                    }

                    @Override
                    public void jump() {
                        isShowVisitorInfoDialog = false;
                        if (!isPlayVideo) {
                            playOrPauseVideo();
                        }
                    }
                });
                portraitVisitorInfoDialog.setCanceledOnTouchOutside(false);
                isVideoShowVisitorInfoDialog = true;
                portraitVisitorInfoDialog.show();
                portraitVisitorInfoDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                });
                playOrPauseVideo();
            }
        }
    }

    private boolean isShowVisitorDialog(int currentPosition) {
        long timeInterval = currentPosition - showVisitorTime;
        if (timeInterval >= 0 && timeInterval < 1000) {
            return true;
        } else {
            return false;
        }
    }

    private void resetVisitorInfo() {
        if (visitorInfos != null && visitorInfos.size() > 0) {
            visitorInfos.removeAll(visitorInfos);
        }
        visitorInfoDialog = null;
        portraitVisitorInfoDialog = null;
        isShowVisitorInfoDialog = false;
    }

    private void hideOnlineOperation() {
        iv_next_video.setVisibility(View.GONE);
        tv_play_definition.setVisibility(View.GONE);
        tv_video_select.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                if (isProjectioning) {
                    projectionBack();
                } else {
                    if (isFullScreen && !isLocalPlay) {
                        setPortrait();
                    } else {
                        finish();
                    }
                }
                break;

            case R.id.iv_video_full_screen:
                if (isFullScreen) {
                    setPortrait();
                } else {
                    setLandScape();
                }
                break;
            case R.id.iv_ad_full_screen:
                if (isFullScreen) {
                    setPortrait();
                } else {
                    setLandScape();
                }
                break;
            case R.id.iv_next_video:
                playNextVideo();
                break;
            case R.id.tv_play_speed:
                hideViews();
                SelectSpeedDialog selectSpeedDialog = new SelectSpeedDialog(activity, currentSpeed, new SelectSpeed() {
                    @Override
                    public void selectedSpeed(float speed) {
                        player.setSpeed(speed);
                        currentSpeed = speed;
                    }
                });
                selectSpeedDialog.show();
                break;
            case R.id.tv_play_definition:
                hideViews();
                selectDefinition();
                break;
            case R.id.tv_video_select:
                hideViews();
                selectVideo();
                break;
            case R.id.iv_play_pause:
                if (isProjectioning) {
                    if (isProjectioningPause) {
                        playProjection();
                    } else {
                        pauseProjection();
                    }
                } else {
                    playOrPauseVideo();
                }

                break;
            case R.id.iv_more_settings:
                showMoreSettings();
                break;
            case R.id.btn_download:
                btn_download.setVisibility(View.GONE);
                ll_confirm_or_cancel.setVisibility(View.VISIBLE);
                isShowSelectButton(true);
                break;
            case R.id.btn_confirm:
                confirmDownload();
                break;
            case R.id.iv_switch_to_audio:
                //音视频互相切换
                switchDefPos = player.getCurrentPosition();
                if (isAudioMode) {
                    isAudioMode = false;
                    playVideoOrAudio(isAudioMode, false);
                } else {
                    isAudioMode = true;
                    playVideoOrAudio(isAudioMode, false);
                }
                break;
            case R.id.btn_cancel:
                btn_download.setVisibility(View.VISIBLE);
                ll_confirm_or_cancel.setVisibility(View.GONE);
                isShowSelectButton(false);
                break;
            case R.id.iv_create_gif:
                //录制gif图
                startCreateGif();
                hideViews();
                break;
            case R.id.iv_save_gif:
                //保存gif图
                MultiUtils.showToast(activity, "gif保存在：" + gifFile.getAbsolutePath());
                cancelGif();
                break;
            case R.id.gif_cancel:
                //取消制作gif
                cancelGif();
                break;
            case R.id.iv_gif_stop:
                //停止录制gif
                stopGif();
                break;
            case R.id.iv_lock_or_unlock:
                if (isLock) {
                    isLock = false;
                    iv_lock_or_unlock.setImageResource(R.mipmap.iv_unlock);
                    showViews();
                } else {
                    isLock = true;
                    iv_lock_or_unlock.setImageResource(R.mipmap.iv_lock);
                    hideViews();
                }
                break;
            case R.id.tv_know_more:
                //点击广告的了解更多
                knowMoreFrontAdInfo();
                break;
            case R.id.tv_skip_ad:
                if (isCanClickAd) {
                    playVIdeoAfterAd();
                }
                break;
            case R.id.iv_pause_ad:
                if (!TextUtils.isEmpty(pauseAdClickUrl)) {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri uri = Uri.parse(pauseAdClickUrl);
                    intent.setData(uri);
                    startActivity(intent);
                    rl_pause_ad.setVisibility(View.GONE);
                }
                break;
            case R.id.tv_close_pause_ad:
                rl_pause_ad.setVisibility(View.GONE);
                break;
            //重新试看
            case R.id.ll_rewatch:
                player.seekTo(0);
                player.start();
                ll_pre_watch_over.setVisibility(View.GONE);
                tv_watch_tip.setVisibility(View.VISIBLE);
                break;
            case R.id.iv_projection_back:
                hideSelectProjectionDevice();
                break;

            case R.id.iv_research:
                SEARCH_DEVICE_TIME = 8;
                ll_not_find_device.setVisibility(View.GONE);
                ll_searching_device.setVisibility(View.VISIBLE);
                ProjectionManager.getInstance().getRegistry().removeAllRemoteDevices();
                getNetworkInfo();
                break;
            case R.id.iv_plus_volume:
                //增加投屏音量
                changeProjectionVolume(true);
                break;
            case R.id.iv_minus_volume:
                //减少投屏音量
                changeProjectionVolume(false);
                break;
            case R.id.iv_portrait_projection:
                showSelectProjectionDevice();
                break;
            case R.id.btn_close_projection:
                ll_connect_projection_fail.setVisibility(View.GONE);
                stopProjection();
                projectionIsOver();
                if (isPrepared) {
                    startVideoTimer();
                }
                break;
            case R.id.iv_projection_screen_back:
                hideProjectionScreenTip();
                break;
            case R.id.iv_small_window_play:
                useSmallWindowPlay();
                break;
            case R.id.iv_landscape_screenshot:
                getVideoScreenShot();
                break;
            case R.id.iv_portrait_screenshot:
                getVideoScreenShot();
                break;
            case R.id.tv_input_danmu:
                sendDanmu(false);
                break;
            case R.id.iv_landscape_danmu_set:
                showDanmuSet();
                break;

            case R.id.ll_portrait_danmu_off:
                openDanmu();
                break;

            case R.id.iv_portrait_danmu_on:
                closeDanmu();
                break;

            case R.id.iv_landscape_danmu_switch:
                if (isDanmuOn) {
                    closeDanmu();
                } else {
                    openDanmu();
                }
                break;
            case R.id.tv_portrait_input_danmu:
                sendDanmu(true);
                break;
            case R.id.iv_portrait_danmu_set:
                showDanmuSet();
                break;

        }
    }

    //打开弹幕
    private void openDanmu() {
        showInputDanmu();
        isDanmuOn = true;
        if (!dm_view.isShown()) {
            dm_view.show();
        }
        resumeDanmu();
        seekToDanmu(currentPosition);
    }

    private void showInputDanmu() {
        ll_portrait_danmu_off.setVisibility(View.GONE);
        ll_portrait_danmu_on.setVisibility(View.VISIBLE);
        ll_landscape_danmu_set_send.setVisibility(View.VISIBLE);
        iv_landscape_danmu_switch.setImageResource(R.mipmap.iv_danmu_on);
    }

    //关闭弹幕
    private void closeDanmu() {
        ll_portrait_danmu_on.setVisibility(View.GONE);
        ll_portrait_danmu_off.setVisibility(View.VISIBLE);
        ll_landscape_danmu_set_send.setVisibility(View.INVISIBLE);
        iv_landscape_danmu_switch.setImageResource(R.mipmap.iv_danmu_off);
        isDanmuOn = false;
        if (dm_view.isShown()) {
            dm_view.hide();
        }
        pauseDanmu();
    }

    //弹幕设置
    private void showDanmuSet() {
        hideViews();
        DanmuSetDialog danmuSetDialog = new DanmuSetDialog(activity, isFullScreen, currentOpaqueness, currentFontSizeLevel, currentDanmuSpeedLevel, currentDisplayArea, new OnDanmuSet() {
            @Override
            public void setOpaqueness(int opaqueness) {
                currentOpaqueness = opaqueness;
                float opaquenessFloat = MultiUtils.calFloat(2, opaqueness, 100);
                danmakuContext.setDanmakuTransparency(opaquenessFloat);
            }

            @Override
            public void setFontSizeLevel(int fontSizeLevel) {
                currentFontSizeLevel = fontSizeLevel;
                if (fontSizeLevel == 0) {
                    fontSizeScale = 0.5f;
                } else if (fontSizeLevel == 1) {
                    fontSizeScale = 1.0f;
                } else if (fontSizeLevel == 2) {
                    fontSizeScale = 1.5f;
                } else if (fontSizeLevel == 3) {
                    fontSizeScale = 2.0f;
                } else if (fontSizeLevel == 4) {
                    fontSizeScale = 2.5f;
                }
                danmakuContext.setScaleTextSize(fontSizeScale);
            }

            @Override
            public void setDanmuSpeed(int danmuSpeedLevel) {
                currentDanmuSpeedLevel = danmuSpeedLevel;
                if (danmuSpeedLevel == 0) {
                    danmuSpeed = 2.0f;
                } else if (danmuSpeedLevel == 1) {
                    danmuSpeed = 1.5f;
                } else if (danmuSpeedLevel == 2) {
                    danmuSpeed = 1.0f;
                } else if (danmuSpeedLevel == 3) {
                    danmuSpeed = 0.75f;
                } else if (danmuSpeedLevel == 4) {
                    danmuSpeed = 0.5f;
                }
                danmakuContext.setScrollSpeedFactor(danmuSpeed);
            }

            @Override
            public void setDisplayArea(int displayArea) {
                currentDisplayArea = displayArea;
                float displayAreaFloat = MultiUtils.calFloat(2, currentDisplayArea, 100);
                int height = rl_play_video.getHeight();
                int danmuViewHeight = (int) (displayAreaFloat * height);
                ViewGroup.LayoutParams layoutParams = rl_danmu.getLayoutParams();
                layoutParams.height = danmuViewHeight;
                rl_danmu.setLayoutParams(layoutParams);

            }
        });
        danmuSetDialog.show();

        danmuSetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (!isFullScreen) {
                    iv_portrait_danmu_set.setImageResource(R.mipmap.iv_danmu_set_gray);
                }
            }
        });
    }

    //发送弹幕
    private void sendDanmu(boolean isPortrait) {
        if (!isCanSendDanmu) {
            return;
        }
        EditDanmuTextDialog editDanmuTextDialog = new EditDanmuTextDialog(activity, isPortrait, new OnEditDanmuText() {
            @Override
            public void getDanmuText(final String danmuText, final String danmuColor) {

                player.sendDanmu(videoId, danmuText, currentPosition, danmuColor, new OnSendDanmuListener() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                isCanSendDanmu = false;
                                sendDanmuInterval = 5;
                                addDanmu(danmuText, danmuColor, currentPosition, true);
                            }
                        });
                    }

                    @Override
                    public void onFail(final String msg) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MultiUtils.showToast(activity, "发送弹幕失败：" + msg);
                            }
                        });
                    }
                });

            }
        });
        editDanmuTextDialog.show();
        if (isPlayVideo && isFullScreen) {
            playOrPauseVideo();
            isPlayAfterSendDanmu = true;
        } else if (isFullScreen) {
            isPlayAfterSendDanmu = false;
        }
        editDanmuTextDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (isPlayAfterSendDanmu && isFullScreen) {
                    playOrPauseVideo();
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MultiUtils.hideSoftKeyboard(tv_video);
                    }
                }, 100);

            }
        });
    }

    //获取视频截图
    private void getVideoScreenShot() {
        String videoScreenShotOutPath = MultiUtils.getVideoScreenShotOutPath();
        if (!TextUtils.isEmpty(videoScreenShotOutPath)) {
            Bitmap bitmap = tv_video.getBitmap();
            boolean videoScreenShot = player.getVideoScreenShot(bitmap, videoScreenShotOutPath);
            if (videoScreenShot) {
                //通知系统相册更新
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(new File(videoScreenShotOutPath))));
                MultiUtils.showToast(activity, "已截图");
            }
        }
    }

    //8.0及以上系统支持小窗播放
    private void useSmallWindowPlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (builder == null) {
                builder = new PictureInPictureParams.Builder();
            }
            int width = rl_play_video.getWidth();
            int height = rl_play_video.getHeight();
            // 设置宽高比例值
            if (width > 0 && height > 0) {
                float whRate = MultiUtils.calFloat(2, width, height);
                if (whRate > 0.42 && whRate < 2.39) {
                    Rational aspectRatio = new Rational(width, height);
                    builder.setAspectRatio(aspectRatio);
                }
            }

            if (actions != null && actions.size() > 0) {
                actions.clear();
            }

            if (player.isPlaying()) {
                actions.add(pauseRemoteAction);
            } else {
                actions.add(playRemoteAction);
            }
            if (actions != null && actions.size() > 0) {
                builder.setActions(actions);
            }

            // 进入小窗模式
            enterPictureInPictureMode(builder.build());
        }

    }

    private void updateSmallWindowActions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (builder == null) {
                builder = new PictureInPictureParams.Builder();
            }
            if (actions != null && actions.size() > 0) {
                actions.clear();
            }

            if (player.isPlaying()) {
                actions.add(pauseRemoteAction);
            } else {
                actions.add(playRemoteAction);
            }
            if (actions != null && actions.size() > 0) {
                builder.setActions(actions);
            }

            setPictureInPictureParams(builder.build());
        }

    }


    class SmallWindowReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int control = intent.getIntExtra("control", 0);
            if (control == 1 || control == 2) {
                playOrPauseVideo();
                updateSmallWindowActions();

            } else if (control == 3) {
                if (isSmallWindow) {
                    finish();
                }
            }
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        isSmallWindow = isInPictureInPictureMode;
        if (isInPictureInPictureMode) {
            if (iv_back.getVisibility() == View.VISIBLE) {
                hideViews();
            }
            if (dm_view.isShown()) {
                dm_view.hide();
            }
        } else {
            if (!dm_view.isShown()) {
                dm_view.show();
            }
        }
    }

    //发送弹幕
    private void addDanmu(String danmuText, String danmuColor, long time, boolean isSend) {
        BaseDanmaku danmaku = danmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        if (danmaku == null || dm_view == null) {
            return;
        }
        danmaku.text = danmuText;
        danmaku.padding = 5;
        danmaku.isLive = false;
        danmaku.priority = 0;
        if (isSend) {
            danmaku.setTime(time + 1500);
        } else {
            danmaku.setTime(time);
        }
        danmaku.textSize = 60f;
        if (!TextUtils.isEmpty(danmuColor) && danmuColor.length() == 8) {
            String textColor = "#" + danmuColor.substring(2, 8);
            if (textColor.length() == 7) {
                //textColor：颜色，格式如：#ffffff
                int color = Color.parseColor(textColor);
                danmaku.textColor = color;
                if (isSend) {
                    danmaku.borderColor = color;
                }
            }
        }
        danmaku.textShadowColor = Color.BLACK;
        dm_view.addDanmaku(danmaku);
    }

    private void seekToDanmu(long time) {
        if (dm_view != null && dm_view.isPrepared() && isDanmuOn) {
            dm_view.seekTo(time);
        }
    }

    private void pauseDanmu() {
        if (dm_view != null && dm_view.isPrepared()) {
            dm_view.pause();
        }
    }

    private void resumeDanmu() {
        if (dm_view != null && dm_view.isPrepared() && dm_view.isPaused()) {
            dm_view.resume();
        }
    }

    private void knowMoreFrontAdInfo() {
        if (!TextUtils.isEmpty(frontADClickUrl)) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri uri = Uri.parse(frontADClickUrl);
            intent.setData(uri);
            startActivity(intent);
        }
    }

    private void hideViews() {
        hideOtherOperations();
        iv_create_gif.setVisibility(View.GONE);
        iv_landscape_screenshot.setVisibility(View.GONE);
        iv_back.setVisibility(View.GONE);
        iv_lock_or_unlock.setVisibility(View.GONE);
    }

    private void showViews() {
        controlHide = 8;
        showOtherOperations();
        if (isFullScreen && !isProjectioning) {
            iv_lock_or_unlock.setVisibility(View.VISIBLE);
        } else {
            iv_lock_or_unlock.setVisibility(View.GONE);
        }

        if (isAudioMode) {
            iv_create_gif.setVisibility(View.GONE);
            iv_landscape_screenshot.setVisibility(View.GONE);
            iv_portrait_screenshot.setVisibility(View.GONE);
        } else {
            if (isFullScreen && !isProjectioning) {
                iv_create_gif.setVisibility(View.VISIBLE);
                iv_landscape_screenshot.setVisibility(View.VISIBLE);
            }
        }
        iv_back.setVisibility(View.VISIBLE);
    }

    //确认下载
    private void confirmDownload() {
        btn_download.setVisibility(View.VISIBLE);
        ll_confirm_or_cancel.setVisibility(View.GONE);
        isShowSelectButton(false);
        int downloadCount = 0;
        int selectedCount = 0;
        for (int i = 0; i < videoList.size(); i++) {
            HuodeVideoInfo videoInfo = videoList.get(i);
            if (videoInfo.isSelectedDownload()) {
                String videoTitle = videoInfo.getVideoTitle();
                String videoCover = videoInfo.getVideoCover();
                //过滤掉本地已存在或正在下载中的文件，不重复下载
                if (!DataSet.hasDownloadInfo(videoTitle)) {
                    DownloadController.insertDownloadInfo(videoInfo.getVideoId(), verificationCode, videoTitle, 0, videoCover);
                    downloadCount++;
                }
                selectedCount++;
            }
        }
        if (downloadCount > 0) {
            MultiUtils.showToast(activity, "文件已加入下载队列");
        } else {
            if (selectedCount > 0) {
                MultiUtils.showToast(activity, "文件已存在");
            }
        }
    }

    private void isShowSelectButton(boolean isSHow) {
        if (!isSHow) {
            if (batchDownload != null && batchDownload.size() > 0) {
                batchDownload.removeAll(batchDownload);
            }
        }
        for (int i = 0; i < videoList.size(); i++) {
            videoList.get(i).setShowSelectButton(isSHow);
            if (isSHow) {
                videoList.get(i).setSelectedDownload(false);
            }
        }
        playListAdapter.notifyDataSetChanged();
    }

    //更多设置
    private void showMoreSettings() {
        int selectedSubtitle = sv_subtitle.getSelectedSubtitle();
        String firstSubName = sv_subtitle.getFirstSubName();
        String secondSubName = sv_subtitle.getSecondSubName();
        MoreSettingsDialog moreSettingsDialog = new MoreSettingsDialog(activity, isAudioMode, currentVideoSizePos, selectedSubtitle, firstSubName, secondSubName, currentBrightness, new MoreSettings() {
            @Override
            public void playAudioOrVideo() {
                if (isLocalPlay) {
                    MultiUtils.showToast(activity, "本地播放不支持切换");
                    return;
                }
                //播放音频
                switchDefPos = player.getCurrentPosition();
                if (isAudioMode) {
                    isAudioMode = false;
                } else {
                    isAudioMode = true;
                }
                playVideoOrAudio(isAudioMode, false);
            }

            @Override
            public void checkNetWork() {
                //网络检测
                if (playInfo != null) {
                    CheckNetworkDialog checkNetworkDialog = new CheckNetworkDialog(activity, videoId, playInfo);
                    checkNetworkDialog.show();
                }
            }

            @Override
            public void downloadVideo() {
                downloadFile();
            }

            @Override
            public void setVideoSize(int position) {
                setSize(position);
            }

            @Override
            public void setSubTitle(int selectedSubtitle) {
                sv_subtitle.setSubtitle(selectedSubtitle);
            }

            @Override
            public void setBrightness(int brightness) {
                currentBrightness = brightness;
            }

            @Override
            public void landScapeProjection() {
                setPortrait();
                showSelectProjectionDevice();
            }

            @Override
            public void smallWindowPlay() {
                useSmallWindowPlay();
            }
        });
        moreSettingsDialog.show();
        hideViews();
        moreSettingsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                showOtherOperations();
            }
        });
    }

    private void showSelectProjectionDevice() {
        if (isPlayVideo) {
            playOrPauseVideo();
        }
        isProjectionContinue = true;
        isGetProjectionVolume = true;
        if (playUrl.contains(".pcm?")) {
            ll_projection_screen.setVisibility(View.VISIBLE);
        } else {
            MultiUtils.setStatusBarColor(activity, R.color.white, true);
            ll_select_projection_device.setVisibility(View.VISIBLE);
            ll_projection_volume.setVisibility(View.VISIBLE);
            getNetworkInfo();
            bindService();
            registerReceivers();
            if (deviceAdapter == null) {
                datas = new ArrayList<>();
                deviceAdapter = new DeviceAdapter(activity, datas);
                lv_device.setAdapter(deviceAdapter);
            }

            getDeviceList();
            // 搜索设备
            registryListener.setOnDeviceListChangedListener(new ProjectionDeviceListChangedListener() {
                @Override
                public void onDeviceAdded(final ProjectionIDevice device) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            datas.add((ProjectionDevice) device);
                            deviceAdapter.notifyDataSetChanged();
                        }
                    });
                }

                @Override
                public void onDeviceRemoved(final ProjectionIDevice device) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            datas.remove(device);
                            deviceAdapter.notifyDataSetChanged();
                        }
                    });
                }
            });

            SEARCH_DEVICE_TIME = 8;
            startSearchDeviceTimer();
        }
    }

    //获取网络情况
    private void getNetworkInfo() {
        int netWorkStatus = MultiUtils.getNetWorkStatus(activity);
        if (netWorkStatus == 0) {
            tv_current_wifi.setText("当前无网络连接");
        } else if (netWorkStatus == 2) {
            tv_current_wifi.setText("当前是手机热点");
        } else {
            String connectWifiName = MultiUtils.getConnectWifiName(activity);
            if (!TextUtils.isEmpty(connectWifiName)) {
                tv_current_wifi.setText("当前WIFI:" + connectWifiName);
            }
        }
    }

    /**
     * 获取投屏设备列表
     */
    private void getDeviceList() {
        Collection<ProjectionDevice> devices = ProjectionManager.getInstance().getDmrDevices();
        ProjectionDeviceList.getInstance().setClingDeviceList(devices);
        if (datas != null) {
            datas.removeAll(datas);
        }
        if (devices != null) {
            datas.addAll(devices);
            deviceAdapter.notifyDataSetChanged();
        }

    }

    private void bindService() {
        Intent serviceIntent = new Intent(
                activity, ProjectionUpnpService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        isBindService = true;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            ProjectionUpnpService.LocalBinder binder = (ProjectionUpnpService.LocalBinder) service;
            ProjectionUpnpService projectionUpnpService = binder.getService();

            ProjectionManager projectionManager = ProjectionManager.getInstance();
            projectionManager.setUpnpService(projectionUpnpService);
            projectionManager.setDeviceManager(new ProjectionDeviceManager());
            projectionManager.getRegistry().addListener(registryListener);
            projectionManager.searchDevices();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            ProjectionManager.getInstance().setUpnpService(null);
        }
    };


    private final class ProjectionHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PLAY_ACTION:
                    iv_play_pause.setImageResource(R.mipmap.iv_pause);
                    isProjectioningPause = false;
                    tv_projection_state.setText("正在投屏播放中");
                    if (currentPosition > 0 && isProjectionContinue) {
                        projectionPlayControl.seek((int) currentPosition, new ProjectionControlCallback() {
                            @Override
                            public void success(ProjectionIResponse response) {
                                isProjectionContinue = false;
                            }

                            @Override
                            public void fail(ProjectionIResponse response) {

                            }
                        });
                    }
                    break;
                case PAUSE_ACTION:
                    isProjectioningPause = true;
                    iv_play_pause.setImageResource(R.mipmap.iv_play);
                    tv_projection_state.setText("已暂停");
                    projectionPlayControl.setCurrentState(ProjectionDLANPlayState.PAUSE);
                    break;
                case STOP_ACTION:
                    if (isPrepared) {
                        startVideoTimer();
                    }
                    projectionIsOver();
                    break;
                case ERROR_ACTION:
                    ll_connect_projection_fail.setVisibility(View.VISIBLE);
                    ll_projection_volume.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    }

    //    接收状态信息
    private class TransportStateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ProjectionIntents.ACTION_PLAYING.equals(action)) {
                mHandler.sendEmptyMessage(PLAY_ACTION);

            } else if (ProjectionIntents.ACTION_PAUSED_PLAYBACK.equals(action)) {
                mHandler.sendEmptyMessage(PAUSE_ACTION);

            } else if (ProjectionIntents.ACTION_STOPPED.equals(action)) {
                mHandler.sendEmptyMessage(STOP_ACTION);

            }
        }
    }

    private void registerReceivers() {
        mTransportStateBroadcastReceiver = new TransportStateBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ProjectionIntents.ACTION_PLAYING);
        filter.addAction(ProjectionIntents.ACTION_PAUSED_PLAYBACK);
        filter.addAction(ProjectionIntents.ACTION_STOPPED);
        filter.addAction(ProjectionIntents.ACTION_TRANSITIONING);
        registerReceiver(mTransportStateBroadcastReceiver, filter);
    }

    //播放投屏视频
    private void playProjection() {
        @ProjectionDLANPlayState.DLANPlayStates int currentState = projectionPlayControl.getCurrentState();

        /**
         * 通过判断状态 来决定 是继续播放 还是重新播放
         */

        if (currentState == ProjectionDLANPlayState.STOP) {
            projectionPlayControl.playNew(playUrl, new ProjectionControlCallback() {
                @Override
                public void success(ProjectionIResponse response) {
                    isProjectioning = true;
                    isProjectioningPause = false;
                    projectionPlayControl.setCurrentState(ProjectionDLANPlayState.PLAY);
                    ProjectionManager.getInstance().registerAVTransport(activity);
                    ProjectionManager.getInstance().registerRenderingControl(activity);
                    startProjectionTimer();
                    mHandler.sendEmptyMessage(PLAY_ACTION);
                }

                @Override
                public void fail(ProjectionIResponse response) {
                    mHandler.sendEmptyMessage(ERROR_ACTION);
                }
            });
        } else {
            projectionPlayControl.play(new ProjectionControlCallback() {
                @Override
                public void success(ProjectionIResponse response) {
                    isProjectioningPause = false;
                    isProjectioning = true;
                    projectionPlayControl.setCurrentState(ProjectionDLANPlayState.PLAY);
                    mHandler.sendEmptyMessage(PLAY_ACTION);
                }

                @Override
                public void fail(ProjectionIResponse response) {
                    mHandler.sendEmptyMessage(ERROR_ACTION);
                }
            });
        }
    }

    //暂停投屏
    private void pauseProjection() {
        mHandler.sendEmptyMessage(PAUSE_ACTION);
        projectionPlayControl.pause(new ProjectionControlCallback() {
            @Override
            public void success(ProjectionIResponse response) {
                isProjectioningPause = true;
                iv_play_pause.setImageResource(R.mipmap.iv_play);
                projectionPlayControl.setCurrentState(ProjectionDLANPlayState.PAUSE);


            }

            @Override
            public void fail(ProjectionIResponse response) {

            }

        });
    }

    //停止投屏
    private void stopProjection() {
        projectionPlayControl.stop(new ProjectionControlCallback() {
            @Override
            public void success(ProjectionIResponse response) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        projectionIsOver();
                    }
                });
            }

            @Override
            public void fail(ProjectionIResponse response) {

            }
        });
    }

    private void projectionIsOver() {
        projectionPlayControl.setCurrentState(ProjectionDLANPlayState.STOP);
        isProjectioning = false;
        rl_projectioning.setVisibility(View.GONE);
        iv_play_pause.setImageResource(R.mipmap.iv_play);
        cancelProjectionTimer();

    }

    //调整投屏音量
    private void changeProjectionVolume(final boolean isPlus) {
        projectionPlayControl.getVolume(new ProjectionControlReceiveCallback() {
            @Override
            public void receive(ProjectionIResponse response) {
                if (isGetProjectionVolume) {
                    ProjectionVolumeResponse projectionVolumeResponse = (ProjectionVolumeResponse) response;
                    volumeValue = projectionVolumeResponse.getResponse();
                    isGetProjectionVolume = false;
                }

                if (isPlus) {
                    volumeValue = volumeValue + 2;
                } else {
                    volumeValue = volumeValue - 2;
                }
                if (volumeValue < 0) {
                    volumeValue = 0;
                }
                projectionPlayControl.setVolume(volumeValue, new ProjectionControlCallback() {
                    @Override
                    public void success(ProjectionIResponse response) {

                    }

                    @Override
                    public void fail(ProjectionIResponse response) {

                    }
                });
            }

            @Override
            public void success(ProjectionIResponse response) {

            }

            @Override
            public void fail(ProjectionIResponse response) {
            }
        });
    }

    //下载文件
    private void downloadFile() {
        dowloadTitle = videoTitle;
        if (!isAudioMode) {
            dowloadTitle = dowloadTitle + "-" + currentDefinition;
            downloadMode = 1;
        } else {
            downloadMode = 2;
        }
        if (DataSet.hasDownloadInfo(dowloadTitle)) {
            MultiUtils.showToast(activity, "文件已存在");
            return;
        }
        if (isAudioMode) {
            DownloadController.insertDownloadInfo(videoId, verificationCode, dowloadTitle, downloadMode, videoCover, 0);
        } else {
            DownloadController.insertDownloadInfo(videoId, verificationCode, dowloadTitle, downloadMode, videoCover, currentDefinition);
        }
        MultiUtils.showToast(activity, "文件已加入下载队列");
    }

    //设置画面尺寸
    private void setSize(int position) {
        currentVideoSizePos = position;
        if (videoHeight > 0) {
            ViewGroup.LayoutParams videoParams = tv_video.getLayoutParams();
            int landVideoHeight = MultiUtils.getScreenHeight(activity);
            int landVideoWidth = landVideoHeight * videoWidth / videoHeight;
            int screenHeight = MultiUtils.getScreenWidth(activity);
            if (landVideoWidth > screenHeight) {
                landVideoWidth = screenHeight;
                landVideoHeight = landVideoWidth * videoHeight / videoWidth;
            }
            if (position == 0) {
                landVideoHeight = MultiUtils.getScreenHeight(activity);
                landVideoWidth = MultiUtils.getScreenWidth(activity);
            } else if (position == 1) {
                landVideoHeight = 1 * landVideoHeight;
                landVideoWidth = 1 * landVideoWidth;
            } else if (position == 2) {
                landVideoHeight = (int) (0.75 * landVideoHeight);
                landVideoWidth = (int) (0.75 * landVideoWidth);
            } else if (position == 3) {
                landVideoHeight = (int) (0.5 * landVideoHeight);
                landVideoWidth = (int) (0.5 * landVideoWidth);
            }
            videoParams.height = landVideoHeight;
            videoParams.width = landVideoWidth;
            tv_video.setLayoutParams(videoParams);
        }
    }

    //暂停或开始播放
    private void playOrPauseVideo() {
        if (player.isPlaying()) {
            player.pause();
            pauseDanmu();
            isPlayVideo = false;
            iv_play_pause.setImageResource(R.mipmap.iv_play);
            //展示暂停广告
            if (pauseADInfoData != null) {
                List<PauseADInfo.AdBean> ad = pauseADInfoData.getAd();
                if (ad != null && ad.get(0) != null) {
                    rl_pause_ad.setVisibility(View.VISIBLE);
                    String material = ad.get(0).getMaterial();
                    pauseAdClickUrl = ad.get(0).getClickurl();
                    Glide.with(HuodeApplication.getContext()).load(material).asBitmap().into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            if (resource != null) {
                                iv_pause_ad.setImageBitmap(resource);
                                int imageWidth = resource.getWidth();
                                int imageHeight = resource.getHeight();
                                int screenWidth = MultiUtils.getScreenWidth(activity);
                                int newWidth = (int) (0.6 * screenWidth);
                                int newHeight = newWidth * imageHeight / imageWidth;
                                ViewGroup.LayoutParams pauseAdParams = rl_pause_ad.getLayoutParams();
                                pauseAdParams.height = newHeight;
                                pauseAdParams.width = newWidth;
                                rl_pause_ad.setLayoutParams(pauseAdParams);
                            }
                        }
                    });

                }
            }
        } else {
            player.start();
            resumeDanmu();
            isPlayVideo = true;
            iv_play_pause.setImageResource(R.mipmap.iv_pause);
            if (rl_pause_ad.getVisibility() == View.VISIBLE) {
                rl_pause_ad.setVisibility(View.GONE);
            }
        }
    }

    //播放视频
    private void startPlay() {
        if (!player.isPlaying()) {
            player.start();
            iv_play_pause.setImageResource(R.mipmap.iv_pause);
        }
    }


    //选集
    private void selectVideo() {
        SelectVideoDialog selectVideoDialog = new SelectVideoDialog(activity, videoList, videoId, new SelectVideo() {
            @Override
            public void selectedVideo(String selectedVideoTitle, String selectedVideoId, String selectedVideoCover) {
                videoId = selectedVideoId;
                videoTitle = selectedVideoTitle;
                videoCover = selectedVideoCover;
                resetInfo();
                playVideoOrAudio(isAudioMode, false);
            }
        });
        selectVideoDialog.show();
        hideOtherOperations();
        selectVideoDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                showOtherOperations();
            }
        });
    }

    //显示视频操作
    private void showOtherOperations() {
        ll_progress_and_fullscreen.setVisibility(View.VISIBLE);
        ll_title_and_audio.setVisibility(View.VISIBLE);
        if (player.isPlaying()) {
            iv_play_pause.setImageResource(R.mipmap.iv_pause);
        }
        if (isProjectioning) {
            ll_title_and_audio.setBackgroundColor(getResources().getColor(R.color.transparent));
            iv_switch_to_audio.setVisibility(View.INVISIBLE);
            iv_small_window_play.setVisibility(View.INVISIBLE);
            iv_portrait_projection.setVisibility(View.INVISIBLE);
            iv_portrait_screenshot.setVisibility(View.INVISIBLE);
        } else {
            if (!isFullScreen) {
                iv_switch_to_audio.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    iv_small_window_play.setVisibility(View.VISIBLE);
                }
                iv_portrait_projection.setVisibility(View.VISIBLE);
                if (!isAudioMode) {
                    iv_portrait_screenshot.setVisibility(View.VISIBLE);
                }
            }
            ll_title_and_audio.setBackgroundColor(getResources().getColor(R.color.play_ope_bac_color));
        }
        iv_back.setVisibility(View.VISIBLE);
    }

    //隐藏视频操作
    private void hideOtherOperations() {
        ll_progress_and_fullscreen.setVisibility(View.INVISIBLE);
        ll_title_and_audio.setVisibility(View.INVISIBLE);
        ll_volume.setVisibility(View.GONE);
        ll_brightness.setVisibility(View.GONE);
        tv_slide_progress.setVisibility(View.GONE);
        iv_portrait_screenshot.setVisibility(View.GONE);
    }

    //切换清晰度
    private void selectDefinition() {
        SelectDefinitionDialog selectDefinitionDialog = new SelectDefinitionDialog(activity, currentDefinition, definitions, new SelectDefinition() {
            @Override
            public void selectedDefinition(String definitionText, int definition) {
                tv_play_definition.setText(definitionText);
                try {
                    currentDefinition = definition;
                    switchDefPos = player.getCurrentPosition();
                    ll_load_video.setVisibility(View.VISIBLE);
                    hideOtherOperations();
                    player.reset();
                    player.setSurface(playSurface);
                    HuodeApplication.getDRMServer().reset();
                    player.setDefaultDefinition(definition);
                    player.setDefinition(activity, definition);
                    player.setSpeed(currentSpeed);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        selectDefinitionDialog.show();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        playSurface = new Surface(surface);
        player.setSurface(playSurface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        if (isGifStart) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastTimeMillis > 100) {
                Bitmap bitmap = tv_video.getBitmap(gifVideoWidth, gifVideoHeight);
                gifMakerThread.addBitmap(bitmap);
                lastTimeMillis = currentTime;
            }
        }
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {

        playInfo = player.getPlayInfo();
        if (playInfo != null) {
            playUrl = playInfo.getPlayUrl();
            currentDefinition = playInfo.getDefaultDefinition();
        }
        isPrepared = true;
        isFirstBuffer = false;
        //切换清晰度续播
        if (switchDefPos > 0) {
            player.seekTo(switchDefPos);
        } else {
            if (lastPlayPosition > 0) {
                if (isPlayFrontAd) {
                    lastPlayPosition = 0;
                }
                if (isPlayFrontAd) {
                    player.start();
                } else {
                    //从上次播放的位置开始播放
                    player.seekTo(lastPlayPosition);
                    danmuSec = (lastPlayPosition / getDanmuInterval) - 1;
                    //从记忆播放处展示课堂练习
                    if (isShowExercise(lastPlayPosition)) {
                        isShowConfirmExerciseDialog = true;
                    } else {
                        isShowConfirmExerciseDialog = false;
                    }
                    returnListenTime = 0;
                }
            } else {
                player.start();
            }
        }
        hidePlayErrorView();
        //得到视频的宽和高
        videoHeight = player.getVideoHeight();
        videoWidth = player.getVideoWidth();

        //设置生成gif图片的分辨率
        if (videoWidth > 320) {
            gifVideoWidth = 320;
            float ratio = videoWidth / 320.0f;
            gifVideoHeight = (int) (videoHeight / ratio);
        } else {
            gifVideoWidth = videoWidth;
            gifVideoHeight = videoHeight;
        }

        if (!isFullScreen) {
            setPortVideo();
        } else {
            //重置画面大小
            setSize(1);
        }
        ll_load_video.setVisibility(View.GONE);
        //视频模式，隐藏音频界面
        if (!isAudioMode) {
            ll_audio_view.setVisibility(View.GONE);
        }
        //获得视频清晰度列表
        if (!isLocalPlay) {
            definitions = player.getDefinitions();
            if (definitions != null) {
                for (String key : definitions.keySet()) {
                    Integer integer = definitions.get(key);
                    if (currentDefinition == integer.intValue()) {
                        tv_play_definition.setText(key);
                    }
                }
            }
        } else {
            setLandScape();
        }
        //设置视频总时长
        videoDuration = player.getDuration();
        tv_video_time.setText(MultiUtils.millsecondsToMinuteSecondStr(videoDuration));
        tv_portrait_video_time.setText(MultiUtils.millsecondsToMinuteSecondStr(videoDuration));
        showInputDanmu();
        //如果正在播放广告
        if (isPlayFrontAd) {
            ll_ad.setVisibility(View.VISIBLE);
            if (!isStartAdTimer) {
                long duration = player.getDuration();
                if ((adTime * 1000) > duration && frontAdCount == 1) {
                    adTime = (int) (duration / 1000);
                }
                startAdTimer();
            }
        } else {
            showOtherOperations();
            //更新播放进度
            startVideoTimer();
            //控制界面的隐藏
            controlHideView();
        }

        //展示视频打点信息
        if (hotSpotDatas != null && hotSpotDatas.size() > 0) {
            sb_progress.setHotSpotPosition(hotSpotDatas, videoDuration / 1000);
            sb_portrait_progress.setHotSpotPosition(hotSpotDatas, videoDuration / 1000);
        }

        //运行跑马灯
        mv_video.start();
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
        switch (what) {
            case DWIjkMediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (isDanmuOn) {
                    pauseDanmu();
                }
                netWorkStatus = MultiUtils.getNetWorkStatus(activity);
                if (netWorkStatus == 0 && !isLocalPlay) {
                    isNoNetPause = true;
                    showPlayErrorView();
                    hideOtherOperations();
                    tv_error_info.setText("请检查你的网络连接");
                    tv_operation.setText("重试");
                    tv_operation.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            hidePlayErrorView();
                            playVideoOrAudio(isAudioMode, false);
                        }
                    });
                } else {
                    ll_load_video.setVisibility(View.VISIBLE);
                }
                break;
            case DWIjkMediaPlayer.MEDIA_INFO_BUFFERING_END:
                if (isDanmuOn) {
                    resumeDanmu();
                }
                if (!isLocalPlay) {
                    isNoNetPause = false;
                }
                ll_load_video.setVisibility(View.GONE);
                break;
        }
        return false;
    }

    //播放错误事件
    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, final int what, int i1) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (!isBackupPlay && !isLocalPlay && isFirstBuffer) {
                    startBackupPlay();
                    return;
                }

                netWorkStatus = MultiUtils.getNetWorkStatus(activity);
                if (netWorkStatus == 0) {
                    isNoNetPause = true;
                }
                tv_error_info.setText("播放出现异常（" + what + "）");
                showPlayErrorView();
                hideOtherOperations();
                if (isLocalPlay) {
                    tv_operation.setVisibility(View.GONE);
                } else {
                    tv_operation.setVisibility(View.VISIBLE);
                }
                tv_operation.setText("重试");
                tv_operation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (netWorkStatus == 0) {
                            MultiUtils.showToast(activity, "请检查你的网络连接");
                            return;
                        }
                        hidePlayErrorView();
                        playVideoOrAudio(isAudioMode, false);
                    }
                });

            }
        });
        return true;
    }

    //获得场景视频自定义错误类型
    @Override
    public void onPlayError(final HuodeException e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (e.getIntErrorCode()) {
                    case 103:
                        tv_error_info.setText("音频无播放节点（" + e.getIntErrorCode() + "）");
                        showPlayErrorView();
                        hideOtherOperations();
                        tv_operation.setText("切换到视频");
                        tv_operation.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                isAudioMode = false;
                                hidePlayErrorView();
                                playVideoOrAudio(isAudioMode, false);
                            }
                        });
                        break;
                    case 102:
                        //切换到音频
                        isAudioMode = true;
                        playVideoOrAudio(isAudioMode, false);
                        break;
                    case 104:
                        tv_error_info.setText("授权验证失败（" + e.getIntErrorCode() + "）");
                        showPlayErrorView();
                        hideOtherOperations();
                        tv_operation.setVisibility(View.GONE);
                        break;
                }
            }
        });

    }

    //网络状态监听
    class NetChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                NetworkInfo dataInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (wifiInfo.isConnected() && dataInfo.isConnected()) {
                    //wifi和移动数据同时连接
                    netWorkStatus = 3;
                } else if (wifiInfo.isConnected() && !dataInfo.isConnected()) {
                    //wifi已连接，移动数据断开
                    netWorkStatus = 1;
                    resumePlay();
                } else if (!wifiInfo.isConnected() && dataInfo.isConnected()) {
                    //wifi断开 移动数据连接
                    netWorkStatus = 2;
                    showIsUseMobileNetwork();
                } else {
                    //wifi断开 移动数据断开
                    netWorkStatus = 0;
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
                        netWorkStatus = 0;
                        break;
                    case 2:
                        //wifi断开 移动数据连接
                        netWorkStatus = 2;
                        showIsUseMobileNetwork();
                        break;
                    case 4:
                        //wifi已连接，移动数据断开
                        netWorkStatus = 1;
                        resumePlay();
                        break;
                    case 5:
                        //wifi和移动数据同时连接
                        netWorkStatus = 3;
                        break;
                }
            }
        }
    }

    private void resumePlay() {
        if (isNoNetPause && !isLocalPlay) {
            if (tv_error_info.getVisibility() == View.VISIBLE) {
                hidePlayErrorView();
            }
            playVideoOrAudio(isAudioMode, false);
        }
    }

    private void startBackupPlay() {
        player.setBackupPlay(true);
        isBackupPlay = true;
        player.reset();
        try {
            if (playSurface != null) {
                player.setSurface(playSurface);
            }
            player.prepareAsync();
        } catch (Exception e) {

        }
    }

    private void showIsUseMobileNetwork() {
        if (isLocalPlay) {
            return;
        }
        if (isShowUseMobie) {
            return;
        }
        IsUseMobileNetworkDialog isUseMobileNetworkDialog = new IsUseMobileNetworkDialog(activity, new IsUseMobieNetwork() {
            @Override
            public void exit() {
                finish();
            }

            @Override
            public void continuePlay() {
                if (tv_error_info.getVisibility() == View.VISIBLE) {
                    hidePlayErrorView();
                }

                isPlayVideo = true;
                iv_play_pause.setImageResource(R.mipmap.iv_pause);
                playVideoOrAudio(isAudioMode, false);
                ll_load_video.setVisibility(View.GONE);
            }
        });
        if (!isUseMobileNetworkDialog.isShowing()) {
            isUseMobileNetworkDialog.show();
        }
        if (isPlayVideo) {
            playOrPauseVideo();
        }
    }

    private void hidePlayErrorView() {
        ll_load_video.setVisibility(View.VISIBLE);
        ll_play_error.setVisibility(View.GONE);
    }

    private void showPlayErrorView() {
        ll_load_video.setVisibility(View.GONE);
        ll_play_error.setVisibility(View.VISIBLE);
    }

    //缓冲进度
    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int percent) {
        sb_progress.setSecondaryProgress(percent);
        sb_portrait_progress.setSecondaryProgress(percent);
    }

    //播放完成
    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {

        if (isSmallWindow) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sendBroadcast(new Intent("com.bokecc.vod.play.SMALL_WINDOW").putExtra("control", 3));
            }
            return;
        }

        if (isLocalPlay) {
            currentPosition = 0;
            updateLastPlayPosition();
            finish();
            return;
        }
        if (isPlayFrontAd) {
            frontAdPosition++;
        }
        if (frontAdPosition < frontAdCount) {
            FrontADInfo.AdBean adBean = frontAd.get(frontAdPosition);
            playFrontAd(adBean);
        } else {
            if (!isPlayFrontAd) {
                //播放下一个视频
                currentPosition = 0;
                playNextVideo();
            }
        }
    }

    //播放下一个视频
    private void playNextVideo() {
        player.resetPlayedAndPausedTime();
        resetInfo();
        playIndex = playIndex + 1;
        if (playIndex >= videoIds.size()) {
            playIndex = 0;
        }
        videoId = videoIds.get(playIndex);
        HuodeVideoInfo videoInfo = videoList.get(playIndex);
        if (videoInfo != null) {
            videoTitle = videoInfo.getVideoTitle();
            videoCover = videoInfo.getVideoCover();
        }
        //记录播放位置
        updateLastPlayPosition();
        isPlayFrontAd = false;
        isCanClickAd = false;
        cancelAdTimer();
        getAdInfo();
    }

    private void resetInfo() {
        // 切换视频，重置questions
        if (questions != null) {
            questions.clear();
            questions = null;
        }
        // 切换视频时，重置打点信息
        if (hotSpotDatas != null) {
            hotSpotDatas.clear();
            sb_progress.clearHotSpots();
            sb_portrait_progress.clearHotSpots();
        }
        //清除访客信息
        resetVisitorInfo();
        //重置课堂练习
        if (exercises != null && exercises.size() > 0) {
            exercises.removeAll(exercises);
        }
        //重置授权验证信息
        isAllowPlayWholeVideo = 2;
        freeWatchTime = 0;
        freeWatchOverMsg = "";
        //重置视频是否展示过访客信息
        isVideoShowVisitorInfoDialog = false;

        sv_subtitle.resetSubtitle();

        //重置播放线路相关
        player.setBackupPlay(false);
        isFirstBuffer = true;
        isBackupPlay = false;
    }

    /**
     * 播放音视频
     *
     * @param isAudioMode 是否是音频模式
     * @param isResetPos  是否重置当前记录播放的位置
     */
    private void playVideoOrAudio(boolean isAudioMode, boolean isResetPos) {
        iv_back.setVisibility(View.VISIBLE);
        if (isResetPos) {
            switchDefPos = 0;
        }
        updateLastPlayPosition();
        isPrepared = false;
        getLastVideoPostion();
        if (isAudioMode) {
            player.setDefaultPlayMode(MediaMode.AUDIO, new OnPlayModeListener() {
                @Override
                public void onPlayMode(MediaMode mediaMode) {

                }
            });
            ll_audio_view.setVisibility(View.VISIBLE);
            tv_play_definition.setVisibility(View.GONE);
            iv_create_gif.setVisibility(View.GONE);
            iv_landscape_screenshot.setVisibility(View.GONE);
            iv_switch_to_audio.setImageResource(R.mipmap.iv_video_mode);
        } else {
            iv_switch_to_audio.setImageResource(R.mipmap.iv_audio_mode);
            if (isFullScreen && !isLocalPlay) {
                iv_more_settings.setVisibility(View.VISIBLE);
            }

            if (!isLocalPlay) {
                tv_play_definition.setVisibility(View.VISIBLE);
            }
            player.setDefaultPlayMode(MediaMode.VIDEO, new OnPlayModeListener() {
                @Override
                public void onPlayMode(MediaMode mediaMode) {

                }
            });
        }
        ll_load_video.setVisibility(View.VISIBLE);
        hideOtherOperations();
        tv_video_title.setText(videoTitle);
        player.pause();
        player.stop();
        player.reset();
        //setClientId()方法在setVideoPlayInfo()前调用，可以通过此方法设置用户标识，出问题时方便排查
        player.setClientId("");
        player.setVideoPlayInfo(videoId, ConfigUtil.USERID, ConfigUtil.API_KEY, verificationCode, activity);
        player.setSurface(playSurface);
        HuodeApplication.getDRMServer().resetLocalPlay();
        player.setSpeed(currentSpeed);
        player.setAudioPlay(isAudioMode);
        player.prepareAsync();

        //更新播放列表正在播放项
        for (int i = 0; i < videoList.size(); i++) {
            HuodeVideoInfo videoInfo = videoList.get(i);
            if (videoInfo != null) {
                if (videoInfo.getVideoId().equals(videoId)) {
                    videoInfo.setSelected(true);
                } else {
                    videoInfo.setSelected(false);
                }
            }
        }
        playListAdapter.notifyDataSetChanged();
    }

    //退出全屏播放
    private void setPortrait() {
        iv_video_full_screen.setVisibility(View.VISIBLE);
        ll_speed_def_select.setVisibility(View.GONE);
        iv_next_video.setVisibility(View.GONE);
        iv_lock_or_unlock.setVisibility(View.GONE);
        iv_create_gif.setVisibility(View.GONE);
        iv_landscape_screenshot.setVisibility(View.GONE);
        iv_more_settings.setVisibility(View.GONE);
        iv_switch_to_audio.setVisibility(View.VISIBLE);
        ll_landscape_progress.setVisibility(View.GONE);
        ll_portrait_progress.setVisibility(View.VISIBLE);
        ll_landscape_danmu.setVisibility(View.GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            iv_small_window_play.setVisibility(View.VISIBLE);
        }
        if (!isAudioMode) {
            iv_portrait_screenshot.setVisibility(View.VISIBLE);
        }
        iv_portrait_projection.setVisibility(View.VISIBLE);
        //小屏播放隐藏打点信息
        sb_progress.setHotspotShown(false);
        sb_portrait_progress.setHotspotShown(false);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        RelativeLayout.LayoutParams videoLayoutParams = (RelativeLayout.LayoutParams) rl_play_video.getLayoutParams();
        videoLayoutParams.topMargin = landScapeMarginTop;
        videoLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        videoLayoutParams.height = landScapeHeight;
        rl_play_video.setLayoutParams(videoLayoutParams);
        //设置竖屏TextureView的宽和高
        setPortVideo();
        isFullScreen = false;
    }

    //设置为全屏播放
    private void setLandScape() {
        iv_video_full_screen.setVisibility(View.GONE);
        ll_speed_def_select.setVisibility(View.VISIBLE);
        if (!isLocalPlay) {
            iv_next_video.setVisibility(View.VISIBLE);
            iv_more_settings.setVisibility(View.VISIBLE);
        }
        iv_switch_to_audio.setVisibility(View.GONE);
        iv_small_window_play.setVisibility(View.GONE);
        iv_portrait_screenshot.setVisibility(View.GONE);
        iv_portrait_projection.setVisibility(View.GONE);

        ll_landscape_progress.setVisibility(View.VISIBLE);
        ll_portrait_progress.setVisibility(View.GONE);
        ll_landscape_danmu.setVisibility(View.VISIBLE);
        if (!isAudioMode && !isPlayFrontAd) {
            iv_create_gif.setVisibility(View.VISIBLE);
            iv_landscape_screenshot.setVisibility(View.VISIBLE);
        }
        if (!isPlayFrontAd) {
            iv_lock_or_unlock.setVisibility(View.VISIBLE);
        }

        if (isProjectioning) {
            ll_speed_def_select.setVisibility(View.GONE);
            iv_next_video.setVisibility(View.GONE);
            iv_lock_or_unlock.setVisibility(View.GONE);
            iv_create_gif.setVisibility(View.GONE);
            iv_landscape_screenshot.setVisibility(View.GONE);
            iv_more_settings.setVisibility(View.GONE);
        }
        //全屏播放展示打点信息
        sb_progress.setHotspotShown(true);
        sb_portrait_progress.setHotspotShown(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        RelativeLayout.LayoutParams videoLayoutParams = (RelativeLayout.LayoutParams) rl_play_video.getLayoutParams();
        landScapeHeight = videoLayoutParams.height;
        landScapeMarginTop = videoLayoutParams.topMargin;
        videoLayoutParams.topMargin = 0;
        videoLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        videoLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        rl_play_video.setLayoutParams(videoLayoutParams);
        setLandScapeVideo();
        isFullScreen = true;
    }

    //设置横屏TextureView的宽和高,使视频高度和屏幕宽度一致
    private void setLandScapeVideo() {
        if (videoHeight > 0) {
            ViewGroup.LayoutParams videoParams = tv_video.getLayoutParams();
            int landVideoHeight = MultiUtils.getScreenWidth(activity);
            int limitedVideoWidth = MultiUtils.getScreenHeight(activity);
            int screenWidth = MultiUtils.getScreenWidth(activity);
            int screenHeight = MultiUtils.getScreenHeight(activity);
            if (screenWidth > screenHeight) {
                landVideoHeight = screenHeight;
                limitedVideoWidth = screenWidth;
            }

            int landVideoWidth = landVideoHeight * videoWidth / videoHeight;
            if (landVideoWidth > limitedVideoWidth) {
                landVideoWidth = limitedVideoWidth;
                landVideoHeight = landVideoWidth * videoHeight / videoWidth;
            }
            videoParams.height = landVideoHeight;
            videoParams.width = landVideoWidth;
            tv_video.setLayoutParams(videoParams);
        }

    }

    //小屏播放时按比例计算宽和高，使视频不变形
    private void setPortVideo() {
        if (videoHeight > 0) {
            ViewGroup.LayoutParams videoParams = tv_video.getLayoutParams();
            int portVideoHeight = MultiUtils.dipToPx(activity, 200);
            int portVideoWidth = portVideoHeight * videoWidth / videoHeight;
            int phoneWidth = 0;
            int screenWidth = MultiUtils.getScreenWidth(activity);
            int screenHeight = MultiUtils.getScreenHeight(activity);
            if (screenWidth > screenHeight) {
                phoneWidth = screenHeight;
            } else {
                phoneWidth = screenWidth;
            }
            if (videoWidth >= phoneWidth) {
                portVideoWidth = phoneWidth;
                portVideoHeight = portVideoWidth * videoHeight / videoWidth;
            }

            videoParams.height = portVideoHeight;
            videoParams.width = portVideoWidth;
            tv_video.setLayoutParams(videoParams);
        }
    }

    //开启更新播放进度任务
    private void startVideoTimer() {
        cancelProjectionTimer();
        cancelVideoTimer();
        timer = new Timer();
        videoTask = new VideoTask();
        timer.schedule(videoTask, 0, 1000);
    }


    //取消更新播放进度任务
    private void cancelVideoTimer() {
        if (timer != null) {
            timer.cancel();
        }
        if (videoTask != null) {
            videoTask.cancel();
        }
    }

    // 播放进度计时器
    class VideoTask extends TimerTask {
        @Override
        public void run() {
            if (MultiUtils.isActivityAlive(activity)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        currentPosition = player.getCurrentPosition();
                        tv_current_time.setText(MultiUtils.millsecondsToMinuteSecondStr(currentPosition));
                        tv_portrait_current_time.setText(MultiUtils.millsecondsToMinuteSecondStr(currentPosition));
                        sb_progress.setProgress((int) currentPosition, (int) videoDuration);
                        sb_portrait_progress.setProgress((int) currentPosition, (int) videoDuration);
                        //更新字幕
                        sv_subtitle.refreshSubTitle(currentPosition);
                        //展示问答题
                        if (isQuestionTimePoint((int) currentPosition) && (qaView == null || !qaView.isPopupWindowShown())) {
                            playOrPauseVideo();
                            showQuestion();
                        }
                        //展示访客信息对话框
                        if (currentPosition > showVisitorTime && isShowVisitorInfoDialog && !isVideoShowVisitorInfoDialog) {
                            player.seekTo((int) showVisitorTime);
                            showVisitorInfoDialog();
                        } else {
                            showVisitorInfoDialog();
                        }

                        //展示课堂练习
                        if (isShowExercise((int) currentPosition)) {
                            if (exeDialog != null && exeDialog.isShowing()) {
                                return;
                            }
                            if (doExerciseDialog != null && doExerciseDialog.isShowing()) {
                                return;
                            }
                            if (!isFullScreen) {
                                setLandScape();
                            }
                            if (isShowConfirmExerciseDialog) {
                                showExercise();
                                return;
                            }
                            showDoExerciseDialog(true);
                        }

                        //如果大于试看时长就暂停
                        if (isAllowPlayWholeVideo == 0 && currentPosition > freeWatchTime * 1000) {
                            player.pause();
                            tv_watch_tip.setVisibility(View.GONE);
                            ll_pre_watch_over.setVisibility(View.VISIBLE);
                            hideViews();
                        }

                        if (qaView != null && qaView.isPopupWindowShown()) {
                            player.pauseWithoutAnalyse(); //针对有的手机上无法暂停，反复调用pause()
                        }

                        //请求弹幕数据，间隔1分钟
                        currentMinutePos = (int) (currentPosition / getDanmuInterval);
                        if (currentMinutePos != danmuSec && isDanmuOn) {
                            player.getDanmuList(videoId, (danmuSec + 1));
                            danmuSec = currentMinutePos;
                        }

                        //发送弹幕间隔
                        if (sendDanmuInterval >= 0 && !isCanSendDanmu) {
                            tv_input_danmu.setText(sendDanmuInterval + "s");
                            tv_portrait_input_danmu.setText(sendDanmuInterval + "s");

                            if (sendDanmuInterval == 0) {
                                isCanSendDanmu = true;
                                tv_input_danmu.setText("弹幕走一波");
                                tv_portrait_input_danmu.setText("点我发弹幕");
                            }
                            sendDanmuInterval--;
                        }

                    }
                });
            }
        }
    }

    //开启网速计时器任务
    private void startNetSpeedTimer() {
        cancelNetSpeedTimer();
        netSpeedTimer = new Timer();
        netSpeedTask = new NetSpeedTask();
        netSpeedTimer.schedule(netSpeedTask, 0, 1000);
    }

    //取消网速计时器任务
    private void cancelNetSpeedTimer() {
        if (netSpeedTimer != null) {
            netSpeedTimer.cancel();
        }
        if (netSpeedTask != null) {
            netSpeedTask.cancel();
        }
    }

    //网速计时器任务
    class NetSpeedTask extends TimerTask {
        @Override
        public void run() {
            if (MultiUtils.isActivityAlive(activity)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        netSpeed = MultiUtils.getNetSpeed(activity.getApplicationInfo().uid);
                        tv_loading.setText("加载中 " + netSpeed);
                    }
                });
            }
        }
    }

    //开启投屏进度任务
    private void startProjectionTimer() {
        cancelVideoTimer();
        cancelProjectionTimer();
        projectionTimer = new Timer();
        projectionTask = new ProjectionTask();
        projectionTimer.schedule(projectionTask, 0, 1000);
    }

    //取消投屏进度任务
    private void cancelProjectionTimer() {

        if (projectionTimer != null) {
            projectionTimer.cancel();
        }
        if (projectionTask != null) {
            projectionTask.cancel();
        }
    }

    //投屏进度计时器
    class ProjectionTask extends TimerTask {

        @Override
        public void run() {
            projectionPlayControl.getPositionInfo(new ProjectionControlReceiveCallback() {
                @Override
                public void receive(ProjectionIResponse response) {
                    ProjectionPositionResponse projectionPositionResponse = (ProjectionPositionResponse) response;
                    PositionInfo positionInfo = projectionPositionResponse.getResponse();
                    final long trackDurationSeconds = positionInfo.getTrackDurationSeconds();
                    final long trackElapsedSeconds = positionInfo.getTrackElapsedSeconds();
                    if (MultiUtils.isActivityAlive(activity)) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (trackDurationSeconds > 0) {
                                    tv_current_time.setText(MultiUtils.millsecondsToMinuteSecondStr((trackElapsedSeconds * 1000)));
                                    tv_portrait_current_time.setText(MultiUtils.millsecondsToMinuteSecondStr((trackElapsedSeconds * 1000)));
                                    sb_progress.setProgress((int) trackElapsedSeconds, (int) trackDurationSeconds);
                                    sb_portrait_progress.setProgress((int) trackElapsedSeconds, (int) trackDurationSeconds);
                                    if (trackElapsedSeconds >= (trackDurationSeconds - 2)) {
                                        mHandler.sendEmptyMessage(STOP_ACTION);
                                    }
                                }


                            }
                        });
                    }
                }

                @Override
                public void success(ProjectionIResponse response) {

                }

                @Override
                public void fail(ProjectionIResponse response) {

                }
            });


        }
    }

    //开启搜寻投屏设备任务
    private void startSearchDeviceTimer() {
        cancelSearchDeviceTimer();
        searchDeviceTimer = new Timer();
        searchDeviceTask = new SearchDeviceTask();
        searchDeviceTimer.schedule(searchDeviceTask, 0, 1000);
    }

    //取消搜寻投屏设备任务
    private void cancelSearchDeviceTimer() {
        if (searchDeviceTimer != null) {
            searchDeviceTimer.cancel();
        }
        if (searchDeviceTask != null) {
            searchDeviceTask.cancel();
        }
    }

    //搜寻投屏设备计时器
    class SearchDeviceTask extends TimerTask {
        @Override
        public void run() {
            if (MultiUtils.isActivityAlive(activity)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SEARCH_DEVICE_TIME--;
                        if (SEARCH_DEVICE_TIME == 0 && datas.size() < 1) {
                            ll_searching_device.setVisibility(View.GONE);
                            ll_not_find_device.setVisibility(View.VISIBLE);
                        } else if (datas.size() > 0) {
                            ll_searching_device.setVisibility(View.VISIBLE);
                            ll_not_find_device.setVisibility(View.GONE);
                        }
                    }
                });
            }
        }
    }

    //展示课堂练习
    private void showExercise() {
        exeDialog = new ShowExeDialog(activity, new ExeOperation() {
            @Override
            public void listenClass() {
                player.seekTo(returnListenTime);
                playOrPauseVideo();
                isShowConfirmExerciseDialog = false;
            }

            @Override
            public void doExe() {
                showDoExerciseDialog(false);
            }

        });
        exeDialog.show();
        if (isPlayVideo) {
            playOrPauseVideo();
        }
    }

    private void showDoExerciseDialog(boolean isChangePlayState) {
        doExerciseDialog = new DoExerciseDialog(activity, exercises.get(0), videoId, new ExercisesContinuePlay() {
            @Override
            public void continuePlay() {
                exercises.remove(0);
                playOrPauseVideo();
            }
        });
        doExerciseDialog.show();
        if (isChangePlayState) {
            playOrPauseVideo();
        }
        boolean isReadExerciseGuide = MultiUtils.getIsReadExerciseGuide();
        if (!isReadExerciseGuide) {
            ExerciseGuideDialog exerciseGuideDialog = new ExerciseGuideDialog(activity);
            exerciseGuideDialog.show();
        }
    }

    //开启广告倒计时
    private void startAdTimer() {
        isStartAdTimer = true;
        adTimer = new Timer();
        adTask = new AdTask();
        adTimer.schedule(adTask, 0, 1000);
    }


    //取消更新播放进度任务
    private void cancelAdTimer() {
        isStartAdTimer = false;
        if (adTimer != null) {
            adTimer.cancel();
        }
        if (adTask != null) {
            adTask.cancel();
        }
    }


    // 广告进度计时器
    class AdTask extends TimerTask {
        @Override
        public void run() {
            if (MultiUtils.isActivityAlive(activity)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (player.isPlaying()) {
                            adTime--;
                            if (skipAdTime != 0) {
                                skipAdTime--;
                            }
                        }
                        if (adTime < 0) {
                            adTime = 0;
                        }
                        tv_ad_countdown.setText("广告剩余" + adTime + "S");
                        if (adTime == 0) {
                            //广告播放完成 播放正片
                            playVIdeoAfterAd();
                        }
                        if (skipAdTime == 0) {
                            isCanClickAd = true;
                            tv_skip_ad.setText("跳过广告");
                        } else {
                            tv_skip_ad.setText(skipAdTime + "S后跳过广告");
                        }
                    }
                });
            }
        }
    }

    private void playVIdeoAfterAd() {
        frontAdPosition++;
        ll_ad.setVisibility(View.GONE);
        playVideoOrAudio(isAudioMode, true);
        cancelAdTimer();
        isPlayFrontAd = false;
    }

    //控制界面的隐藏
    private void controlHideView() {
        cancelControlHideView();
        controlHide = 8;
        hideTimer = new Timer();
        controlHideTask = new controlHideTask();
        hideTimer.schedule(controlHideTask, 0, 1000);
    }

    //取消控制界面的隐藏
    private void cancelControlHideView() {
        if (hideTimer != null) {
            hideTimer.cancel();
        }
        if (controlHideTask != null) {
            controlHideTask.cancel();
        }
    }

    // 控制界面的隐藏计时器
    class controlHideTask extends TimerTask {
        @Override
        public void run() {
            if (MultiUtils.isActivityAlive(activity)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        controlHide = controlHide - 1;
                        if (controlHide == 0) {
                            hideViews();
                        }
                    }
                });
            }
        }
    }

    //QA问答
    private void createQuestionMap(JSONArray qaJsonArray) {
        for (int i = 0; i < qaJsonArray.length(); i++) {
            try {
                Question question = new Question(qaJsonArray.getJSONObject(i));
                questions.put(question.getShowTime(), question);
            } catch (JSONException e) {

            }
        }
    }

    private boolean isQuestionTimePoint(int currentPosition) {
        if (questions == null || questions.size() < 1) {
            return false;
        }

        int questionTimePoint = questions.firstKey().intValue() * 1000; //需要换算成毫秒

        return currentPosition >= questionTimePoint;
    }

    QAView.QAViewDismissListener myQAViewDismissListener = new QAView.QAViewDismissListener() {

        @Override
        public void seeBackPlay(int backplay, boolean isRight) {
            player.seekTo(backplay * 1000);
            playOrPauseVideo();
            if (isRight) {
                questions.remove(questions.firstKey());
            }
        }

        @Override
        public void continuePlay() {
            playOrPauseVideo();
            questions.remove(questions.firstKey());
        }

        @Override
        public void jumpQuestion() {
            playOrPauseVideo();
            questions.remove(questions.firstKey());
        }
    };

    private void showQuestion() {
        if (qaView != null && qaView.isPopupWindowShown()) {
            return;
        }

        if (qaView == null) {
            qaView = new QAView(this, videoId);

            qaView.setQAViewDismissListener(myQAViewDismissListener);
        }

        qaView.setQuestion(questions.firstEntry().getValue());
        qaView.show(getWindow().getDecorView().findViewById(android.R.id.content));
    }

    //课堂练习
    private boolean isShowExercise(int currentPosition) {
        if (exercises == null || exercises.size() < 1) {
            return false;
        }

        //需要换算成毫秒
        exerciseTimePoint = exercises.get(0).getShowTime() * 1000;
        return currentPosition >= exerciseTimePoint;
    }

    //开始生成gif
    private void startCreateGif() {
        if (gifMakerThread != null && gifMakerThread.isAlive()) {
            MultiUtils.showToast(activity, "处理中，请稍候");
            return;
        }

        gifFile = new File(Environment.getExternalStorageDirectory().getPath() + "/" + ConfigUtil.DOWNLOAD_PATH + "/" + System.currentTimeMillis() + ".gif");

        if (!player.isPlaying()) {
            playOrPauseVideo();
        }

        ivGifShow.setImageBitmap(null);
        ivGifStop.setImageResource(R.mipmap.iv_gif_stop_can_not_use);
        ivGifStop.setVisibility(View.VISIBLE);

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

    GifMakerThread.GifMakerListener gifMakerListener = new GifMakerThread.GifMakerListener() {

        @Override
        public void onGifFinish() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isGifFinish = true;
                    playOrPauseVideo();
                    gifTips.setVisibility(View.GONE);
                    ll_show_gif.setVisibility(View.VISIBLE);
                    ivGifShow.setVisibility(View.VISIBLE);
                    ivGifStop.setVisibility(View.GONE);
                    gifProgressView.setVisibility(View.GONE);
                    Glide.with(SpeedPlayActivity.this).load(gifFile).asGif().diskCacheStrategy(DiskCacheStrategy.NONE).into(ivGifShow);
                }
            });

        }

        @Override
        public void onGifError(final Exception e) {
            MultiUtils.showToast(SpeedPlayActivity.this, "制作gif出错");
        }
    };

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
                            ivGifStop.setImageResource(R.mipmap.iv_gif_stop_can_use);
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

    // 停止获取新的gif帧
    private void stopGif() {
        if (gifRecordTime < gifMin) {
            return;
        }
        endCreateGif();
    }

    //在cancelGif的时候直接调用
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
        ll_show_gif.setVisibility(View.GONE);
        startPlay();
    }

    private void setGifViewStatus(int status) {
        ivGifStop.setVisibility(status);
        gifProgressView.setVisibility(status);
        gifTips.setVisibility(status);
        gifCancel.setVisibility(status);
        ivGifShow.setVisibility(status);
    }

    //更新本地数据库记录的播放位置
    private void updateLastPlayPosition() {
        if (!TextUtils.isEmpty(videoId) && lastVideoPosition != null && isPrepared && !isPlayFrontAd) {
            lastVideoPosition.setPosition((int) currentPosition);
            videoPositionDBHelper.updateVideoPosition(lastVideoPosition);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isAudioMode) {
            player.pause();
        }
        pauseDanmu();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isPrepared && !isAudioMode && isPlayVideo) {
            player.start();
        }
        resumeDanmu();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        HuodeApplication.getDRMServer().disconnectCurrentStream();
        updateLastPlayPosition();
        cancelVideoTimer();
        cancelControlHideView();
        cancelAdTimer();
        cancelProjectionTimer();
        cancelSearchDeviceTimer();
        cancelNetSpeedTimer();
        if (player != null) {
            player.pause();
            player.stop();
            player.release();
        }

        if (netReceiver != null) {
            unregisterReceiver(netReceiver);
        }

        if (smallWindowReceiver != null) {
            unregisterReceiver(smallWindowReceiver);
        }

        if (dm_view != null) {
            dm_view.release();
            dm_view = null;
        }

        if (isBindService) {
            unbindService(serviceConnection);
            ProjectionManager.getInstance().destroy();
            ProjectionManager.getInstance().destroy();
        }

        if (mTransportStateBroadcastReceiver != null) {
            unregisterReceiver(mTransportStateBroadcastReceiver);
        }
        mHandler.removeCallbacksAndMessages(null);
        if (isProjectioning) {
            stopProjection();
        }

        sensorManager.unregisterListener(this);
    }

    //返回事件监听
    @Override
    public void onBackPressed() {
        if (!isGifCancel) {
            cancelGif();
            return;
        }
        if (isLock) {
            return;
        }
        if (ll_select_projection_device.getVisibility() == View.VISIBLE) {
            hideSelectProjectionDevice();
            return;
        }
        if (ll_projection_screen.getVisibility() == View.VISIBLE) {
            hideProjectionScreenTip();
            return;
        }


        if (isProjectioning) {
            projectionBack();
        } else {
            if (isFullScreen && !isLocalPlay) {
                setPortrait();
            } else {
                super.onBackPressed();
            }
        }
    }

    private void hideProjectionScreenTip() {
        ll_projection_screen.setVisibility(View.GONE);
        playOrPauseVideo();
        MultiUtils.setStatusBarColor(this, R.color.black, false);
    }

    private void hideSelectProjectionDevice() {
        ll_select_projection_device.setVisibility(View.GONE);
        cancelSearchDeviceTimer();
        playOrPauseVideo();
        MultiUtils.setStatusBarColor(this, R.color.black, false);
    }

    //投屏过程中返回调用
    private void projectionBack() {
        if (isFullScreen) {
            setPortrait();
        } else {
            if (isPrepared) {
                iv_play_pause.setImageResource(R.mipmap.iv_play);
                startVideoTimer();
            }
            stopProjection();
        }
    }

    //重力感应旋转屏幕
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == null) {
            return;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            int x = (int) event.values[0];
            int y = (int) event.values[1];
            int z = (int) event.values[2];

            long sensorTime = System.currentTimeMillis();

            int absX = Math.abs(mX - x);
            int absY = Math.abs(mY - y);
            int absZ = Math.abs(mZ - z);

            int maxvalue = MultiUtils.getMaxValue(absX, absY, absZ);
            if (maxvalue > 2 && (sensorTime - lastSensorTime) > 1000) {
                lastSensorTime = sensorTime;
                if (isFullScreen) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                }
            }
            mX = x;
            mY = y;
            mZ = z;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN
                || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {

            if (ll_volume.getVisibility() == View.VISIBLE) {
                ll_volume.setVisibility(View.GONE);
            }
        }
        return super.dispatchKeyEvent(event);
    }


}
