package com.bokecc.vod.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bokecc.vod.R;
import com.bokecc.vod.inter.MoreSettings;
import com.bokecc.vod.utils.MultiUtils;

public class MoreSettingsDialog extends Dialog {
    private Context context;
    private MoreSettings moreSettings;
    private int currentVideoSizePos, selectedSubtitle,currentBrightness;
    private String firstSubName, secondSubName;
    private AudioManager audioManager;
    private boolean isAudioMode;

    public MoreSettingsDialog(Context context,boolean isAudioMode,int currentVideoSizePos, int selectedSubtitle,
                              String firstSubName,String secondSubName,int currentBrightness, MoreSettings moreSettings) {
        super(context, R.style.SetVideoDialog);
        this.context = context;
        this.isAudioMode = isAudioMode;
        this.moreSettings = moreSettings;
        this.currentVideoSizePos = currentVideoSizePos;
        this.selectedSubtitle = selectedSubtitle;
        this.firstSubName = firstSubName;
        this.secondSubName = secondSubName;
        this.currentBrightness = currentBrightness;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_more_settings, null);
        setContentView(view);

        LinearLayout ll_play_audio = view.findViewById(R.id.ll_play_audio);
        LinearLayout ll_check_network = view.findViewById(R.id.ll_check_network);
        LinearLayout ll_landscape_projection_screen = view.findViewById(R.id.ll_landscape_projection_screen);
        LinearLayout ll_download_video = view.findViewById(R.id.ll_download_video);
        LinearLayout ll_video_size = view.findViewById(R.id.ll_video_size);
        LinearLayout ll_subtitle = view.findViewById(R.id.ll_subtitle);
        TextView tv_entire_screen = view.findViewById(R.id.tv_entire_screen);
        TextView tv_one_hundred_percent = view.findViewById(R.id.tv_one_hundred_percent);
        TextView tv_seventy_five_percent = view.findViewById(R.id.tv_seventy_five_percent);
        TextView tv_fifty_percent = view.findViewById(R.id.tv_fifty_percent);
        TextView tv_double_subtitles = view.findViewById(R.id.tv_double_subtitles);
        TextView tv_first_subtitle = view.findViewById(R.id.tv_first_subtitle);
        TextView tv_second_subtitle = view.findViewById(R.id.tv_second_subtitle);
        TextView tv_close_subtitle = view.findViewById(R.id.tv_close_subtitle);
        TextView tv_no_subtitles = view.findViewById(R.id.tv_no_subtitles);
        SeekBar sb_brightness = view.findViewById(R.id.sb_brightness);
        SeekBar sb_volume = view.findViewById(R.id.sb_volume);
        ImageView iv_switch_play_mode = view.findViewById(R.id.iv_switch_play_mode);
        TextView tv_play_mode = view.findViewById(R.id.tv_play_mode);
        TextView tv_video_size = view.findViewById(R.id.tv_video_size);
        TextView tv_subtitle = view.findViewById(R.id.tv_subtitle);
        if (isAudioMode){
            iv_switch_play_mode.setImageResource(R.mipmap.iv_video_mode_big);
            tv_play_mode.setText("视频播放");
            tv_video_size.setVisibility(View.GONE);
            ll_video_size.setVisibility(View.GONE);
            tv_subtitle.setVisibility(View.GONE);
            ll_subtitle.setVisibility(View.GONE);
        }else {
            iv_switch_play_mode.setImageResource(R.mipmap.iv_audio_mode_big);
            tv_play_mode.setText("音频播放");
        }

        ll_landscape_projection_screen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreSettings.landScapeProjection();
                dismiss();
            }
        });

        ll_play_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreSettings.playAudioOrVideo();
                dismiss();
            }
        });

        ll_check_network.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreSettings.checkNetWork();
                dismiss();
            }
        });

        ll_download_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreSettings.downloadVideo();
                dismiss();
            }
        });

        //画面尺寸
        tv_entire_screen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreSettings.setVideoSize(0);
                dismiss();
            }
        });

        tv_one_hundred_percent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreSettings.setVideoSize(1);
                dismiss();
            }
        });

        tv_seventy_five_percent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreSettings.setVideoSize(2);
                dismiss();
            }
        });

        tv_fifty_percent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreSettings.setVideoSize(3);
                dismiss();
            }
        });

        switch (currentVideoSizePos) {
            case 0:
                tv_entire_screen.setTextColor(context.getResources().getColor(R.color.orange));
                break;
            case 1:
                tv_one_hundred_percent.setTextColor(context.getResources().getColor(R.color.orange));
                break;
            case 2:
                tv_seventy_five_percent.setTextColor(context.getResources().getColor(R.color.orange));
                break;
            case 3:
                tv_fifty_percent.setTextColor(context.getResources().getColor(R.color.orange));
                break;
        }

        //字幕设置
        if (TextUtils.isEmpty(firstSubName) || TextUtils.isEmpty(secondSubName)){
            tv_double_subtitles.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(firstSubName)){
            tv_first_subtitle.setVisibility(View.GONE);
        }else {
            tv_first_subtitle.setText(firstSubName);
        }

        if (TextUtils.isEmpty(secondSubName)){
            tv_second_subtitle.setVisibility(View.GONE);
        }else {
            tv_second_subtitle.setText(secondSubName);
        }

        if (TextUtils.isEmpty(firstSubName) && TextUtils.isEmpty(secondSubName)){
            tv_close_subtitle.setVisibility(View.GONE);
            tv_no_subtitles.setVisibility(View.VISIBLE);
        }

        tv_first_subtitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreSettings.setSubTitle(0);
                dismiss();
            }
        });

        tv_second_subtitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreSettings.setSubTitle(1);
                dismiss();
            }
        });

        tv_double_subtitles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreSettings.setSubTitle(2);
                dismiss();
            }
        });

        tv_close_subtitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreSettings.setSubTitle(3);
                dismiss();
            }
        });

        if (selectedSubtitle==0){
            tv_first_subtitle.setTextColor(context.getResources().getColor(R.color.orange));
        }else if (selectedSubtitle == 1){
            tv_second_subtitle.setTextColor(context.getResources().getColor(R.color.orange));
        }else if (selectedSubtitle == 2){
            tv_double_subtitles.setTextColor(context.getResources().getColor(R.color.orange));
        }else if (selectedSubtitle == 3){
            tv_close_subtitle.setTextColor(context.getResources().getColor(R.color.orange));
        }

        //调整亮度
        sb_brightness.setProgress(currentBrightness);
        sb_brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                MultiUtils.setBrightness((Activity) context,progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                moreSettings.setBrightness(seekBar.getProgress());
            }
        });

        //音量调节
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        sb_volume.setMax(maxVolume);
        sb_volume.setProgress(currentVolume);

        sb_volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics();
        lp.width = (int) (d.widthPixels * 0.45);
        lp.height = (int) (d.heightPixels * 1.0);
        dialogWindow.setAttributes(lp);
        dialogWindow.setGravity(Gravity.RIGHT);
    }

}
