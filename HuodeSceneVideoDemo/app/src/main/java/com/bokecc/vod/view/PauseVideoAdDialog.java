package com.bokecc.vod.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bokecc.sdk.mobile.util.HttpUtil;
import com.bokecc.vod.R;
import com.bokecc.vod.utils.MultiUtils;

import java.io.IOException;

public class PauseVideoAdDialog extends Dialog implements TextureView.SurfaceTextureListener, MediaPlayer.OnPreparedListener {
    private Context context;
    private boolean isFullScreen;
    private String material;
    private String clickUrl;
    private TextureView tv_video;
    private MediaPlayer player;
    private Surface playSurface;
    private int videoHeight, videoWidth, adVideoHeight, adVideoWidth;
    private boolean isVideoAdSoundOn = false, isPrepared = false;

    public PauseVideoAdDialog(Context context, boolean isFullScreen, String material, String clickUrl) {
        super(context, R.style.ExerciseGuideDialog);
        this.context = context;
        this.isFullScreen = isFullScreen;
        this.material = material;
        this.clickUrl = clickUrl;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_pause_video_ad, null);
        setContentView(view);

        tv_video = view.findViewById(R.id.tv_video);
        player = new MediaPlayer();
        player.setOnPreparedListener(this);
        player.setLooping(true);
        player.setVolume(0.0f, 0.0f);
        tv_video.setSurfaceTextureListener(this);
        try {
            player.setDataSource(material);
            player.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final ImageView iv_video_ad_sound = view.findViewById(R.id.iv_video_ad_sound);
        iv_video_ad_sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isVideoAdSoundOn) {
                    player.setVolume(0.0f, 0.0f);
                    iv_video_ad_sound.setImageResource(R.mipmap.iv_video_ad_sound_off);
                    isVideoAdSoundOn = false;
                } else {
                    player.setVolume(1.0f, 1.0f);
                    iv_video_ad_sound.setImageResource(R.mipmap.iv_video_ad_sound_on);
                    isVideoAdSoundOn = true;
                }
            }
        });

        ImageView iv_close_pause_video_ad = view.findViewById(R.id.iv_close_pause_video_ad);
        iv_close_pause_video_ad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        tv_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(clickUrl)) {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri uri = Uri.parse(clickUrl);
                    intent.setData(uri);
                    context.startActivity(intent);
                }
            }
        });

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics();
        lp.width = (int) (d.widthPixels * 0.01);
        lp.height = (int) (d.heightPixels * 0.01);
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        dialogWindow.setDimAmount(0f);
        dialogWindow.setAttributes(lp);
        if (isFullScreen) {
            dialogWindow.setGravity(Gravity.CENTER);
        } else {
            dialogWindow.setGravity(Gravity.TOP);
        }
        setCanceledOnTouchOutside(false);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (player != null) {
            player.pause();
            player.stop();
            player.release();
            isPrepared = false;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        playSurface = new Surface(surfaceTexture);
        player.setSurface(playSurface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        isPrepared = true;
        player.start();
        videoWidth = player.getVideoWidth();
        videoHeight = player.getVideoHeight();
        updateView(isFullScreen);
    }

    public void updateView(boolean isFullScreen) {
        if (videoHeight > 0 && videoWidth > 0) {
            if (!isFullScreen) {
                adVideoHeight = MultiUtils.dipToPx(context, 112);
                adVideoWidth = adVideoHeight * videoWidth / videoHeight;
                int maxPortVideoAdWidth = MultiUtils.dipToPx(context, 200);
                if (adVideoWidth > maxPortVideoAdWidth) {
                    adVideoWidth = maxPortVideoAdWidth;
                    adVideoHeight = adVideoWidth * videoHeight / videoWidth;
                }
            } else {
                adVideoHeight = MultiUtils.dipToPx(context, 225);
                adVideoWidth = adVideoHeight * videoWidth / videoHeight;

                int maxLandScapeVideoAdWidth = MultiUtils.dipToPx(context, 400);
                if (adVideoWidth > maxLandScapeVideoAdWidth) {
                    adVideoWidth = maxLandScapeVideoAdWidth;
                    adVideoHeight = adVideoWidth * videoHeight / videoWidth;
                }
            }
            ViewGroup.LayoutParams videoParams = tv_video.getLayoutParams();
            videoParams.width = adVideoWidth;
            videoParams.height = adVideoHeight;
            tv_video.setLayoutParams(videoParams);

            Window dialogWindow = getWindow();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            lp.width = adVideoWidth;
            lp.height = adVideoHeight;
            if (!isFullScreen) {
                int offset = (MultiUtils.dipToPx(context, 200) - adVideoHeight) / 2;
                lp.y = offset;
                dialogWindow.setGravity(Gravity.TOP);
            } else {
                lp.y = 0;
                dialogWindow.setGravity(Gravity.CENTER);
            }
            dialogWindow.setAttributes(lp);
        }
    }

    public void pauseVideoAd() {
        if (isPrepared) {
            player.pause();
        }
    }

    public void resumeVideoAd() {
        if (isPrepared) {
            player.start();
        }
    }

}
