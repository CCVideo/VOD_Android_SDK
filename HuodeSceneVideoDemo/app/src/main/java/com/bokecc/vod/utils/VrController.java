package com.bokecc.vod.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MD360DirectorFactory;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.common.MDDirection;
import com.asha.vrlib.model.BarrelDistortionConfig;
import com.asha.vrlib.model.MDHitEvent;
import com.asha.vrlib.model.MDHotspotBuilder;
import com.asha.vrlib.model.MDPinchConfig;
import com.asha.vrlib.model.MDPosition;
import com.asha.vrlib.model.MDRay;
import com.asha.vrlib.model.MDViewBuilder;
import com.asha.vrlib.model.position.MDMutablePosition;
import com.asha.vrlib.plugins.MDAbsPlugin;
import com.asha.vrlib.plugins.hotspot.IMDHotspot;
import com.asha.vrlib.plugins.hotspot.MDAbsHotspot;
import com.asha.vrlib.plugins.hotspot.MDAbsView;
import com.asha.vrlib.plugins.hotspot.MDSimpleHotspot;
import com.asha.vrlib.plugins.hotspot.MDView;
import com.asha.vrlib.strategy.projection.AbsProjectionStrategy;
import com.asha.vrlib.strategy.projection.IMDProjectionFactory;
import com.asha.vrlib.strategy.projection.MultiFishEyeProjection;
import com.asha.vrlib.texture.MD360BitmapTexture;
import com.bokecc.sdk.mobile.play.DWMediaPlayer;
import com.bokecc.vod.R;
import com.bokecc.vod.view.SeekBarHoverView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.squareup.picasso.MemoryPolicy.NO_CACHE;
import static com.squareup.picasso.MemoryPolicy.NO_STORE;


/**
 * Created by cc on 2017/4/27.
 */

public class VrController {

    private MDVRLibrary mVRLibrary;

    public static final int STATUS_PLAYING = 1;
    public static final int STATUS_PAUSE = 0;

    private int seekBarMax = 600;

    private String startPauseTitle = "start_pause";
    private String muteTitle = "mute";
    private String soundNormalTitle = "sound_normal";
    private String brightnessTitle = "brightness_normal";
    private String backTitle = "back";
    private String expandTitle = "expand";
    private String collapseTitle = "collpase";
    private String currentTimeViewTitle = "currentTimeViewTitle";
    private String durationViewTitle = "durationViewTitle";

    private String tagCurrentTime = "current_time";
    private String tagSeekBar = "seek_bar";

    // vr控制监听器
    public interface VrControllerListener {

        /**
         * 播放器状态改变，即暂停和开始
         * @param status
         */
        void onPlayerStatusChanged(int status);

        /**
         * 播放器声音变换回调
         * @param progress
         */
        void onPlayerSoundChanged(int progress);

        /**
         * 清晰度变化
         * @param position
         */
        void onDefinitionChanged(int position);

        /**
         * surfaceview点击回调
         */
        void onViewClick();

        /**
         * 眼控进度回调
         * @param progress
         */
        void onEyeHitProgressUpdate(float progress);

        /**
         * 返回回调
         */
        void onBackPressed();

    }

    private VrControllerListener listener;
    public void setVrControllerListener(VrControllerListener listener) {
        this.listener = listener;
    }

    private DWMediaPlayer mPlayer;
    private Activity activity;

    public VrController(Activity activity, DWMediaPlayer player, int glViewResId) {
        this.activity = activity;
        mPlayer = player;
        mVRLibrary = createVRLibrary(glViewResId);
        initBrightness();
        initOnEyeListener();
    }


    protected MDVRLibrary createVRLibrary(int glViewResId) {
        return MDVRLibrary.with(activity)
                .displayMode(MDVRLibrary.DISPLAY_MODE_NORMAL)
                .interactiveMode(MDVRLibrary.INTERACTIVE_MODE_MOTION)
                .asVideo(new MDVRLibrary.IOnSurfaceReadyCallback() {
                    @Override
                    public void onSurfaceReady(Surface surface) {
                        mPlayer.setSurface(surface);
                    }
                })
                .ifNotSupport(new MDVRLibrary.INotSupportCallback() {
                    @Override
                    public void onNotSupport(int mode) {
                        String tip = mode == MDVRLibrary.INTERACTIVE_MODE_MOTION
                                ? "onNotSupport:MOTION" : "onNotSupport:" + String.valueOf(mode);

                        Log.e("vr", tip);
                    }
                })
                .pinchConfig(new MDPinchConfig().setMin(1.0f).setMax(8.0f).setDefaultValue(0.1f))
                .pinchEnabled(true)
                .directorFactory(new MD360DirectorFactory() {
                    @Override
                    public MD360Director createDirector(int index) {
                        return MD360Director.builder().setPitch(90).build();
                    }
                })
                .listenGesture(new MDVRLibrary.IGestureListener() {
                    @Override
                    public void onClick(MotionEvent e) {
                        listener.onViewClick();
                    }
                })
                .projectionFactory(new CustomProjectionFactory())
                .barrelDistortionConfig(new BarrelDistortionConfig().setDefaultEnabled(false).setScale(0.95f))
                .build(glViewResId);
    }

    int currentBrightness = 100;
    private void initBrightness() {

        try {
            // 系统亮度值范围：0～255，应用窗口亮度范围：0.0f～1.0f。
            currentBrightness = android.provider.Settings.System.getInt(
                    activity.getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS);

            double temp = (double)currentBrightness / 20;

            //向上取证，为了展示亮度，比如初始亮度为71，则需要处理为80
            currentBrightness = (int)Math.ceil(temp) * 20;
            setBrightness(currentBrightness);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean isMute = false;
    boolean isHide = false;

    //初始化眼控监听器
	private void initOnEyeListener() {
        getVRLibrary().setEyePickChangedListener(new MDVRLibrary.IEyePickListener2() {
            @Override
            public void onHotspotHit(MDHitEvent hitEvent) {
            	
            	if (!isPrepared) {
            		return;
            	}

                IMDHotspot hotspot = hitEvent.getHotspot();
                MDRay ray = hitEvent.getRay();

                //设置显示眼控面板的显示范围
                if (ray.getDir().getY() > 0.5f && ray.getDir().getX() < 0.5f && ray.getDir().getZ() > -0.4f) {

                	if (isHide) {
                		if (isControllerShown) {
                        	reloadAllPlugins();
                        } else {
                        	clearPluginInfo();
                            initExpandCollapsePlugin();
                            getVRLibrary().addPlugin(expandCollapsePlugins.get(1));
                            initBackPlugin();
                        }
                		isHide = false;
                	}
                	
                } else {
                	
                	if (!isHide) {
                		removeAllPlugin();
                		isHide = true;
                	}
                }
                
            	if (hotspot == null) {
            		listener.onEyeHitProgressUpdate(0);
                    return;
            	}
            	
            	String text = hotspot.getTitle();
                // 如果是不需要眼控监测的
                if (text.equals(bgTitle) || text.equals(brightnessTitle) || text.equals(durationViewTitle) || text.equals(currentTimeViewTitle)) {
                	listener.onEyeHitProgressUpdate(0);
                    return;
                }
                
                long timeDiff = System.currentTimeMillis() - hitEvent.getTimestamp();
                listener.onEyeHitProgressUpdate((float)timeDiff / 1000);
                
                if (timeDiff > VrConfig.EYE_HIT_MAX_TIME){
                	
                    getVRLibrary().resetEyePick();

                    //是否是开始、暂停的图片
                    if (startPauseTitle.equals(text)) {
                        if (mPlayer.isPlaying()) {
                            listener.onPlayerStatusChanged(STATUS_PAUSE);
                        } else {
                            listener.onPlayerStatusChanged(STATUS_PLAYING);
                        }
                        return;
                    }

                    // 静音，需要切换成普通声音
                    if (muteTitle.equals(text)) {
                        isMute = false;
                        if (currentVolume == 0) {
                            listener.onPlayerSoundChanged(maxVolume / 5);
                        } else {
                            listener.onPlayerSoundChanged(currentVolume); //恢复为上一个记录的数值
                        }

                        return;
                    }

                    // 普通声音，需要切换成静音
                    if (soundNormalTitle.equals(text)) {
                        isMute = true;
                        listener.onPlayerSoundChanged(0);
                        return;
                    }
                    
                    //展开、收起
                    if (expandTitle.equals(text)) {
                        isControllerShown = false;
                        clearPluginInfo();
                        initExpandCollapsePlugin();
                        getVRLibrary().addPlugin(expandCollapsePlugins.get(1));
                        initBackPlugin();
                        return;
                    }
                    
                    if (collapseTitle.equals(text)) {
                    	isControllerShown = true;
                        reloadAllPlugins();
                        return;
                    }
                    
                    //返回键
                    if (backTitle.equals(text)) {
                        listener.onBackPressed();
                        return;
                    }
                    
                    // 清晰度
                    if (definitionList.indexOf(text) > -1 && definitionList.size() > 1) {
                        int position = definitionList.indexOf(text);
                        changeDefinitionView(position);
                        listener.onDefinitionChanged(position);
                        return;
                    }

                    //TODO 回调声音
                    //是否是声音控制的图片
                    if (soundList.indexOf(text) > -1) {
                        listener.onPlayerSoundChanged((soundList.indexOf(text) + 1) * maxVolume / 5);
                        return;
                    }

                    //是否是亮度控制的时间
                    if (brightnessList.indexOf(text) > -1) {
                        changeBrightness((brightnessList.indexOf(text) + 1) * 20);
                        setBrightness((brightnessList.indexOf(text) + 1) * 20);
                        return;
                    }
                }
            }
        });
    }

    // 重新加载所有插件
    private void reloadAllPlugins() {
    	clearPluginInfo();
        initViewPlugin();
        initImagePlugin();
        initDefinitionView(definitionIndex);
    }

    public MDVRLibrary getVRLibrary() {
        return mVRLibrary;
    }

    // video大小变化
    public void onVideoSizeChanged(int width, int height) {
        getVRLibrary().onTextureResize(width, height);
    }

    private boolean isCurrentPlaying = true;

	public void changeStartPausePlugins(boolean isCurrentPlaying) {
    	
    	this.isCurrentPlaying = isCurrentPlaying;
    	if (!isVr || !isControllerShown) {
    		return;
    	}
    	
        removePlugins(startPausePlugins);
        if (isCurrentPlaying) {
            getVRLibrary().addPlugin(startPausePlugins.get(0));
        } else {
            getVRLibrary().addPlugin(startPausePlugins.get(1));
        }
    }

    public void changeSound(int currentVolume) {
    	
        if (currentVolume == 0) {
        	isMute = true;
        } else {
        	isMute = false;
        }
    	
        if (this.currentVolume == 0 && currentVolume == 0 && isMute) {
            return;
        }

        //如果静音的话，保留非静音的数值，当再次点击静音的时候，可以恢复到上一个声音的数值
        if(!isMute) {
            this.currentVolume = currentVolume;
        }
        
        if (!isVr || !isControllerShown) {
    		return;
    	}

        removePlugins(muteNormalPlugins);
        addSoundTagPlugin(currentVolume);

        removePlugins(soundPlugins);
        addSoundPlugin(currentVolume);
    }

    public void changeBrightness(int currentBrightness) {
    	if (!isVr || !isControllerShown) {
    		return;
    	}
        removePlugins(brightnessPlugins);
        addBrightnessPlugin(currentBrightness);
    }
    
    public void changeDefinitionView(int index) {
    	definitionIndex = index;
    	
    	if (!isVr || !isControllerShown) {
    		return;
    	}
    	
        for (int i=0; i< definitionList.size(); i++) {
            MDAbsView definitionView = getVRLibrary().findViewByTag(definitionList.get(i));
            if (definitionView != null){
                TextView textView = definitionView.castAttachedView(TextView.class);
                if (i == index) {
                    textView.setTextColor(activity.getResources().getColor(R.color.rb_text_check));
                } else {
                    textView.setTextColor(Color.WHITE);
                }
                definitionView.invalidate();
            }
        }
    }

    private List<MDAbsPlugin> plugins = new LinkedList<>();
    private List<MDAbsPlugin> soundPlugins = new LinkedList<>();
    private List<MDAbsPlugin> brightnessPlugins = new LinkedList<>();
    private List<MDAbsPlugin> startPausePlugins = new LinkedList<>();
    private List<MDAbsPlugin> muteNormalPlugins = new LinkedList<>();
    private List<MDAbsPlugin> expandCollapsePlugins = new LinkedList<>();

//    private float oriX = 2.0f, oriZ = -1.0f, oriY = -10.0f;
//    private float oriX = 0.0f, oriZ = 0.0f, oriY = -8.0f, oriYaw = -90.0f;
    private float oriX = 0.3f, oriZ = -10.0f, oriY = -8.0f, oriYaw = -75.0f;

    // 让背景置于最下边
    private MDPosition logoPosition = MDMutablePosition.newInstance().setYaw(oriYaw).setY(oriY + 1.0f).setZ(oriZ + 0.5f).setX(oriX);

    private MDVRLibrary.IImageLoadProvider mImageLoadProvider = new ImageLoadProvider();

    private float textSizeX = 1.5f, textSizeY = 1.0f;
    
    private float imageSizeX = 0.8f, imageSizeY = 0.8f;
    
    private String bgTitle = "backgroud";

    private void initViewPlugin() {
    	
    	if (!isControllerShown) {
    		return;
    	}
    	
        MDHotspotBuilder builder = MDHotspotBuilder.create(mImageLoadProvider)
                .size(7.02f, 4.85f)
                .provider(activity, R.drawable.vr_bg)
                .title(bgTitle)
                .position(logoPosition);
        MDAbsHotspot hotspot = new MDSimpleHotspot(builder);
        plugins.add(hotspot);
        getVRLibrary().addPlugin(hotspot);


        TextView textViewCurrentTime = new TextView(activity);
        textViewCurrentTime.setText("00:00:00");
        textViewCurrentTime.setTextColor(Color.WHITE);
        textViewCurrentTime.setGravity(Gravity.CENTER);
        MDPosition textViewCurrentTimePosition = MDPosition.newInstance().setYaw(oriYaw).setY(oriY).setZ(oriZ - 0.8f).setX(oriX - 2.9f);
        addViewPlugin(textViewCurrentTime, 400, 100, textSizeX + 0.2f, textSizeY + 0.2f, textViewCurrentTimePosition, currentTimeViewTitle, tagCurrentTime);

        TextView textViewDuration = new TextView(activity);
        textViewDuration.setText(MultiUtils.millsecondsToStr((int)mPlayer.getDuration()));
        textViewDuration.setTextColor(Color.WHITE);
        textViewDuration.setGravity(Gravity.CENTER);
        MDPosition textViewDurationPosition = MDPosition.newInstance().setYaw(oriYaw).setY(oriY).setZ(oriZ - 0.8f).setX(oriX + 2.9f);
        addViewPlugin(textViewDuration, 400, 100, textSizeX + 0.2f, textSizeY + 0.2f, textViewDurationPosition, durationViewTitle, "tagDuration");

        ProgressBar seekBar = new SeekBarHoverView(activity, null, android.R.attr.progressBarStyleHorizontal);
        seekBar.setMax(seekBarMax);
        seekBar.setOnHoverListener(new View.OnHoverListener() {

            private float lastX;
            private boolean isSeek = false;
            @Override
            public boolean onHover(View view, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
                    isSeek = false;
                }

                if (Math.abs(event.getX() - lastX) < 10) {
                    if (event.getEventTime() - event.getDownTime() > VrConfig.EYE_SEEKBAR_HOVER_TIME && !isSeek) {

                        mPlayer.seekTo((int)(event.getX() * mPlayer.getDuration() / seekBarMax));

                        isSeek = true;
                    }

                } else {
                    lastX = event.getX();
                }

                return false;
            }
        });

        seekBar.setProgressDrawable(activity.getResources().getDrawable(R.drawable.qs_progress_bg));
        MDPosition seekbarPosition = MDPosition.newInstance().setYaw(oriYaw).setY(oriY).setZ(oriZ - 2.5f).setX(oriX);

        addViewPlugin(seekBar, seekBarMax, 100, textSizeX + 2.2f, 0.12f, seekbarPosition, "seekView", tagSeekBar);
    }

    ArrayList<String> definitionList;
    int definitionIndex;
    public void initDefinitionText(Map<String, Integer> definitionMap, int index) {
        definitionList = new ArrayList<>(definitionMap.keySet());
        definitionIndex = index;
        initDefinitionView(index);
    }
    
    private void initDefinitionView(int index) {
    	
    	if (!isControllerShown) {
    		return;
    	}
    	
    	int size = definitionList.size();
    	float step = -0.8f * (size - 1);
        for (int i=0; i<size; i++) {
            TextView textViewNormal = new TextView(activity);
            textViewNormal.setText(definitionList.get(i));
            if (i == index) {
                textViewNormal.setTextColor(activity.getResources().getColor(R.color.rb_text_check));
            } else {
                textViewNormal.setTextColor(Color.WHITE);
            }

            textViewNormal.setGravity(Gravity.CENTER);
            MDPosition textViewNormalPosition = MDPosition.newInstance().setYaw(oriYaw).setY(oriY).setZ(oriZ + 0.0f).setX(oriX + step);
            addViewPlugin(textViewNormal, 200, 100, textSizeX - 0.2f, textSizeY - 0.2f, textViewNormalPosition, definitionList.get(i), definitionList.get(i));

            step = step + 1.6f;
        }
    }

    private void addViewPlugin(View view, int viewWidth, int viewHeight,
                               float sizeX, float sizeY,
                               MDPosition position, String title, String tag) {

        MDViewBuilder builder = MDViewBuilder.create()
                .provider(view, viewWidth, viewHeight)
                .size(sizeX, sizeY)
                .position(position)
                .title(title)
                .tag(tag);

        MDAbsView mdView = new MDView(builder);
        plugins.add(mdView);
        getVRLibrary().addPlugin(mdView);
    }

    private void addImagePlugin(float sizeX, float sizeY, int resId, String title, MDPosition position, List<MDAbsPlugin> plugins) {
        MDHotspotBuilder builder = MDHotspotBuilder.create(mImageLoadProvider)
                .size(sizeX, sizeY)
                .provider(activity, resId)
                .title(title)
                .position(position);
        MDAbsHotspot hotspot = new MDSimpleHotspot(builder);
        plugins.add(hotspot);
    }

    private void removePlugins(List<MDAbsPlugin> list) {
        for (MDAbsPlugin plugin: list) {
            getVRLibrary().removePlugin(plugin);
        }
    }

    public void removeAllPlugin() {
        getVRLibrary().removePlugins();
    }

    public void updateCurrentTime(long currentTime) {
            MDAbsView mdViewCurrentTime = getVRLibrary().findViewByTag(tagCurrentTime);
            if (mdViewCurrentTime != null){
                TextView textView = mdViewCurrentTime.castAttachedView(TextView.class);
                textView.setText(MultiUtils.millsecondsToStr((int)currentTime));
                mdViewCurrentTime.invalidate();
            }

            MDAbsView mdViewSeekBar = getVRLibrary().findViewByTag(tagSeekBar);
            if (mdViewSeekBar != null){
                ProgressBar seekbar = mdViewSeekBar.castAttachedView(ProgressBar.class);
                if (mPlayer.getDuration() == 0) {
                	return;
                }
                seekbar.setProgress((int)(currentTime * seekBarMax / mPlayer.getDuration()));
                mdViewSeekBar.invalidate();
            }
    }

    public void onResume() {
        mVRLibrary.onResume(activity);
    }

    public void onPause() {
        mVRLibrary.onPause(activity);
    }

    public void onDestroy() {
        mVRLibrary.onDestroy();
        activity = null;
    }

    public void onConfigurationChanged() {
        mVRLibrary.onOrientationChanged(activity);
    }

    boolean isPrepared = false;
    public VrController onPrepared() {
        if (getVRLibrary() != null) {
            getVRLibrary().notifyPlayerChanged();
        }
        
        if (!isVr) {
        	return this;
        }
        
        isPrepared = true;

        clearPluginInfo();
        initViewPlugin();
        initImagePlugin();
        
        return this;
    }

    // 清楚插件信息
    private void clearPluginInfo() {
        removeAllPlugin();
        plugins.clear();
        soundPlugins.clear();
        brightnessPlugins.clear();
        startPausePlugins.clear();
        muteNormalPlugins.clear();
        expandCollapsePlugins.clear();
    }

    private float stepY = 0.4f;
    private float stepX = 0.1f;

    // 初始化bitmap图片
    private void initImagePlugin() {
    	if (isControllerShown) {
    		// 初始化亮度和声音刻度插件
            initBrightAndSoundPlugin();
            if (isMute) {
            	addSoundPlugin(0);
            } else {
            	addSoundPlugin(currentVolume);
            }
            
            addBrightnessPlugin(currentBrightness);

            // 初始化声音tag
            initSoundTagPlugin();
            if (isMute) {
            	addSoundTagPlugin(0);
            } else {
            	addSoundTagPlugin(currentVolume);
            }
            

            // 初始化亮度tag
            initBrightnessTagPlugin();

            // 初始化暂停开始图片
            initStartPauseImagePlugin();
            if (isCurrentPlaying) {
                getVRLibrary().addPlugin(startPausePlugins.get(0));
            } else {
                getVRLibrary().addPlugin(startPausePlugins.get(1));
            }
    	}

        // 初始化展开收起图片
        initExpandCollapsePlugin();
        
        if (isControllerShown) {
        	getVRLibrary().addPlugin(expandCollapsePlugins.get(0));
        } else {
        	getVRLibrary().addPlugin(expandCollapsePlugins.get(1));
        }

        // 初始化返回图片
        initBackPlugin();
    }
    
    private void addSoundTagPlugin(int currentVolume) {
        if (currentVolume == 0) {
            getVRLibrary().addPlugin(muteNormalPlugins.get(0));
            isMute = true;
        } else {
            getVRLibrary().addPlugin(muteNormalPlugins.get(1));
            isMute = false;
        }
    }

    private void initStartPauseImagePlugin() {
        MDPosition pausePosition = MDPosition.newInstance().setYaw(oriYaw).setY(oriY).setZ(oriZ - 1.0f).setX(oriX);
        addImagePlugin(0.8f, 0.8f, R.drawable.vr_pause, startPauseTitle, pausePosition, startPausePlugins);
        addImagePlugin(0.8f, 0.8f, R.drawable.vr_start, startPauseTitle, pausePosition, startPausePlugins);
    }

    private void initSoundTagPlugin() {
        MDPosition soundTagPosition = MDPosition.newInstance().setYaw(oriYaw).setY(oriY).setZ(oriZ + 0.4f).setX(oriX + 2.4f);
        addImagePlugin(imageSizeX, imageSizeY, R.drawable.vr_sound_mute, muteTitle, soundTagPosition, muteNormalPlugins);
        addImagePlugin(imageSizeX, imageSizeY, R.drawable.vr_sound_normal, soundNormalTitle, soundTagPosition, muteNormalPlugins);
    }

    private void initBrightnessTagPlugin() {
        MDPosition brightnessTagPosition = MDPosition.newInstance().setYaw(oriYaw).setY(oriY).setZ(oriZ + 0.6f).setX(oriX - 2.4f);
        addImagePlugin(imageSizeX, imageSizeY, R.drawable.vr_brightness_tag, brightnessTitle, brightnessTagPosition, plugins);

        getVRLibrary().addPlugin(plugins.get(plugins.size()-1));
    }
    
    private void initExpandCollapsePlugin() {
    	MDPosition ecPosition = MDPosition.newInstance().setYaw(oriYaw).setY(oriY).setZ(oriZ + 1.8f).setX(oriX + 1.0f);
    	addImagePlugin(imageSizeX, imageSizeY, R.drawable.collapse, expandTitle, ecPosition, expandCollapsePlugins);
        addImagePlugin(imageSizeX, imageSizeY, R.drawable.expand, collapseTitle, ecPosition, expandCollapsePlugins);
    }
    
    private void initBackPlugin() {
        MDPosition backPosition = MDPosition.newInstance().setYaw(oriYaw).setY(oriY).setZ(oriZ + 1.8f).setX(oriX - 1.0f);
        addImagePlugin(imageSizeX, imageSizeY, R.drawable.vr_back, backTitle, backPosition, plugins);
        getVRLibrary().addPlugin(plugins.get(plugins.size()-1));
    }

    private void addSoundPlugin(int currentVolume) {

        int soundIndex = 0;
        if (currentVolume == 0) {
            soundIndex = -1;
        } else if (currentVolume == maxVolume) {
            soundIndex = 4;
        } else {
            soundIndex = (currentVolume - 1) * 5 / maxVolume;
        }

        for (int i=0; i<5; i++) {
            if (i <= soundIndex) {
                getVRLibrary().addPlugin(soundPlugins.get(i * 2));
            } else {
                getVRLibrary().addPlugin(soundPlugins.get(i * 2 + 1));
            }
        }
    }

    private void addBrightnessPlugin(int currentBrightness) {

        int brightnessIndex = 0;
        if (currentBrightness == 0) {
            brightnessIndex = -1;
        } else if (currentBrightness == 100) {
            brightnessIndex = 4;
        } else {
            brightnessIndex = (currentBrightness - 1) * 5 / 100;
        }

        for (int i=0; i<5; i++) {
            if (i <= brightnessIndex) {
                getVRLibrary().addPlugin(brightnessPlugins.get(i * 2));
            } else {
                getVRLibrary().addPlugin(brightnessPlugins.get(i * 2 + 1));
            }
        }
    }

    private String[] brightnessTitleArray = new String[] {
            "brightness1",
            "brightness2",
            "brightness3",
            "brightness4",
            "brightness5",
    };

    private String[] soundTitleArray = new String[] {
            "sound1",
            "sound2",
            "sound3",
            "sound4",
            "sound5",
    };

    List<String> soundList = Arrays.asList(soundTitleArray);
    List<String> brightnessList = Arrays.asList(brightnessTitleArray);

    private void initBrightAndSoundPlugin() {
        MDPosition brightness5Position = MDPosition.newInstance().setYaw(oriYaw).setY(oriY).setZ(oriZ + 1.5f).setX(oriX - 3.4f);
        addImagePlugin(sizeImageX, sizeImageY, R.drawable.brightness_5_enable, brightnessTitleArray[0], brightness5Position, brightnessPlugins);
        addImagePlugin(sizeImageX, sizeImageY, R.drawable.brightness_5_disable, brightnessTitleArray[0], brightness5Position, brightnessPlugins);

        MDPosition brightness4Position = MDPosition.newInstance().setYaw(oriYaw).setY(oriY).setZ(oriZ + 1.5f - stepY * 1).setX(oriX - 3.4f);
        addImagePlugin(sizeImageX, sizeImageY, R.drawable.brightness_4_enable, brightnessTitleArray[1], brightness4Position, brightnessPlugins);
        addImagePlugin(sizeImageX, sizeImageY, R.drawable.brightness_4_disable, brightnessTitleArray[1], brightness4Position, brightnessPlugins);

        MDPosition brightness3Position = MDPosition.newInstance().setYaw(oriYaw).setY(oriY).setZ(oriZ + 1.5f - stepY * 2).setX(oriX - 3.4f + 0.05f);
        addImagePlugin(sizeImageX, sizeImageY, R.drawable.brightness_3_enable, brightnessTitleArray[2], brightness3Position, brightnessPlugins);
        addImagePlugin(sizeImageX, sizeImageY, R.drawable.brightness_3_disable, brightnessTitleArray[2], brightness3Position, brightnessPlugins);

        MDPosition brightness2Position = MDPosition.newInstance().setYaw(oriYaw).setY(oriY).setZ(oriZ + 1.5f - stepY * 3).setX(oriX - 3.4f + 0.10f);
        addImagePlugin(sizeImageX, sizeImageY, R.drawable.brightness_2_enable, brightnessTitleArray[3], brightness2Position, brightnessPlugins);
        addImagePlugin(sizeImageX, sizeImageY, R.drawable.brightness_2_disable, brightnessTitleArray[3], brightness2Position, brightnessPlugins);

        MDPosition brightness1Position = MDPosition.newInstance().setYaw(oriYaw).setY(oriY).setZ(oriZ + 1.5f - stepY * 4).setX(oriX - 3.4f + 0.25f);
        addImagePlugin(sizeImageX, sizeImageY, R.drawable.brightness_1_enable, brightnessTitleArray[4], brightness1Position, brightnessPlugins);
        addImagePlugin(sizeImageX, sizeImageY, R.drawable.brightness_1_disable, brightnessTitleArray[4], brightness1Position, brightnessPlugins);


        MDPosition sound5Position = MDPosition.newInstance().setYaw(oriYaw).setY(oriY).setZ(oriZ + 1.5f).setX(oriX + 3.4f);
        addImagePlugin(sizeImageX, sizeImageY, R.drawable.sound_5_enable, soundTitleArray[0], sound5Position, soundPlugins);
        addImagePlugin(sizeImageX, sizeImageY, R.drawable.sound_5_disable, soundTitleArray[0], sound5Position, soundPlugins);

        MDPosition sound4Position = MDPosition.newInstance().setYaw(oriYaw).setY(oriY).setZ(oriZ + 1.5f - stepY * 1).setX(oriX + 3.4f);
        addImagePlugin(sizeImageX, sizeImageY, R.drawable.sound_4_enable, soundTitleArray[1], sound4Position, soundPlugins);
        addImagePlugin(sizeImageX, sizeImageY, R.drawable.sound_4_disable, soundTitleArray[1], sound4Position, soundPlugins);

        MDPosition sound3Position = MDPosition.newInstance().setYaw(oriYaw).setY(oriY).setZ(oriZ + 1.5f - stepY * 2).setX(oriX + 3.4f - 0.05f);
        addImagePlugin(sizeImageX, sizeImageY, R.drawable.sound_3_enable, soundTitleArray[2], sound3Position, soundPlugins);
        addImagePlugin(sizeImageX, sizeImageY, R.drawable.sound_3_disable, soundTitleArray[2], sound3Position, soundPlugins);

        MDPosition sound2Position = MDPosition.newInstance().setYaw(oriYaw).setY(oriY).setZ(oriZ + 1.5f - stepY * 3).setX(oriX + 3.4f - 0.10f);
        addImagePlugin(sizeImageX, sizeImageY, R.drawable.sound_2_enable, soundTitleArray[3], sound2Position, soundPlugins);
        addImagePlugin(sizeImageX, sizeImageY, R.drawable.sound_2_disable, soundTitleArray[3], sound2Position, soundPlugins);

        MDPosition sound1Position = MDPosition.newInstance().setYaw(oriYaw).setY(oriY).setZ(oriZ + 1.5f - stepY * 4).setX(oriX + 3.4f - 0.25f);
        addImagePlugin(sizeImageX, sizeImageY, R.drawable.sound_1_enable, soundTitleArray[4], sound1Position, soundPlugins);
        addImagePlugin(sizeImageX, sizeImageY, R.drawable.sound_1_disable, soundTitleArray[4], sound1Position, soundPlugins);
    }

    private float sizeImageX = 0.3f;
    private float sizeImageY = 0.3f;

    // picasso impl
    private class ImageLoadProvider implements MDVRLibrary.IImageLoadProvider{

        private SimpleArrayMap<Uri,Target> targetMap = new SimpleArrayMap<>();

        @Override
        public void onProvideBitmap(final Uri uri, final MD360BitmapTexture.Callback callback) {

            final Target target = new Target() {

                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    // texture
                    callback.texture(bitmap);
                    targetMap.remove(uri);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    targetMap.remove(uri);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {}
            };
            targetMap.put(uri, target);
            Picasso.with(activity).load(uri).resize(callback.getMaxTextureSize(),callback.getMaxTextureSize()).onlyScaleDown().centerInside().memoryPolicy(NO_CACHE, NO_STORE).into(target);
        }
    }

    public class CustomProjectionFactory implements IMDProjectionFactory {

        public static final int CUSTOM_PROJECTION_FISH_EYE_RADIUS_VERTICAL = 9611;

        @Override
        public AbsProjectionStrategy createStrategy(int mode) {
            switch (mode){
                case CUSTOM_PROJECTION_FISH_EYE_RADIUS_VERTICAL:
                    return new MultiFishEyeProjection(0.745f, MDDirection.VERTICAL);
                default:return null;
            }
        }
    }

    int maxVolume;
    int currentVolume;
    public VrController initAudio(int maxVolume, int currentVolume) {
        this.maxVolume = maxVolume;
        this.currentVolume = currentVolume;
        
        if (currentVolume == 0) {
        	isMute = true;
        } else {
        	isMute = false;
        }
        
        return this;
    }

    private void setBrightness(int progress) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = Float.valueOf(progress) * (1f / 100f); //设置范围0f~1.0f
        activity.getWindow().setAttributes(lp);
    }

    public void switchInteractiveMode(int interactiveMode) {
        mVRLibrary.switchInteractiveMode(activity, interactiveMode);
    }

    public void switchDisplayMode(int displayMode) {
        mVRLibrary.switchDisplayMode(activity, displayMode);
    }

    private boolean isVr;
	public VrController setIsVr(boolean isVr) {
		this.isVr = isVr;
		return this;
	}
	
	private boolean isControllerShown = false;
}
