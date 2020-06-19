package com.bokecc.vod.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
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
import com.bokecc.vod.inter.OnDanmuSet;

import java.util.logging.Level;

public class DanmuSetDialog extends Dialog {
    private Context context;
    private OnDanmuSet onDanmuSet;
    private int currentOpaqueness;
    private int currentFontSizeLevel = 2;
    private int currentDanmuSpeedLevel = 2;
    private int currentDisplayArea;
    private boolean isFullScreen = false;

    public DanmuSetDialog(Context context,boolean isFullScreen, int currentOpaqueness, int currentFontSizeLevel, int currentDanmuSpeedLevel,int currentDisplayArea, OnDanmuSet onDanmuSet) {
        super(context, R.style.SetVideoDialog);
        this.context = context;
        this.isFullScreen = isFullScreen;
        this.currentOpaqueness = currentOpaqueness;
        this.currentFontSizeLevel = currentFontSizeLevel;
        this.currentDanmuSpeedLevel = currentDanmuSpeedLevel;
        this.currentDisplayArea = currentDisplayArea;
        this.onDanmuSet = onDanmuSet;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_danmu_set, null);
        setContentView(view);

        ImageView iv_danmu_set_back = view.findViewById(R.id.iv_danmu_set_back);
        iv_danmu_set_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        SeekBar sb_opaqueness = view.findViewById(R.id.sb_opaqueness);
        final TextView tv_opaqueness = view.findViewById(R.id.tv_opaqueness);
        sb_opaqueness.setProgress(currentOpaqueness);
        sb_opaqueness.setMax(100);
        tv_opaqueness.setText(currentOpaqueness + "%");
        sb_opaqueness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tv_opaqueness.setText(i + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (onDanmuSet != null) {
                    onDanmuSet.setOpaqueness(progress);
                }
            }
        });

        final SeekBar sb_font_size = view.findViewById(R.id.sb_font_size);
        final TextView tv_font_size = view.findViewById(R.id.tv_font_size);
        sb_font_size.setMax(100);
        if (currentFontSizeLevel == 0) {
            sb_font_size.setProgress(0);
            tv_font_size.setText("超小");
        } else if (currentFontSizeLevel == 1) {
            sb_font_size.setProgress(25);
            tv_font_size.setText("小");
        } else if (currentFontSizeLevel == 2) {
            sb_font_size.setProgress(50);
            tv_font_size.setText("正常");
        } else if (currentFontSizeLevel == 3) {
            sb_font_size.setProgress(75);
            tv_font_size.setText("大");
        } else if (currentFontSizeLevel == 4) {
            sb_font_size.setProgress(100);
            tv_font_size.setText("超大");
        }

        sb_font_size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (progress >= 0 && progress < 13) {
                    sb_font_size.setProgress(0);
                    currentFontSizeLevel = 0;
                    tv_font_size.setText("超小");
                } else if (progress >= 13 && progress < 25) {
                    sb_font_size.setProgress(25);
                    currentFontSizeLevel = 1;
                    tv_font_size.setText("小");
                } else if (progress >= 25 && progress < 38) {
                    sb_font_size.setProgress(25);
                    currentFontSizeLevel = 1;
                    tv_font_size.setText("小");
                } else if (progress >= 38 && progress < 50) {
                    sb_font_size.setProgress(50);
                    currentFontSizeLevel = 2;
                    tv_font_size.setText("正常");
                } else if (progress >= 50 && progress < 63) {
                    sb_font_size.setProgress(50);
                    currentFontSizeLevel = 2;
                    tv_font_size.setText("正常");
                } else if (progress >= 63 && progress < 75) {
                    sb_font_size.setProgress(75);
                    currentFontSizeLevel = 3;
                    tv_font_size.setText("大");
                } else if (progress >= 75 && progress < 88) {
                    sb_font_size.setProgress(75);
                    currentFontSizeLevel = 3;
                    tv_font_size.setText("大");
                } else if (progress >= 88 && progress <= 100) {
                    sb_font_size.setProgress(100);
                    currentFontSizeLevel = 4;
                    tv_font_size.setText("超大");
                }

                if (onDanmuSet != null) {
                    onDanmuSet.setFontSizeLevel(currentFontSizeLevel);
                }
            }
        });

        final SeekBar sb_danmu_speed = view.findViewById(R.id.sb_danmu_speed);
        final TextView tv_danmu_speed = view.findViewById(R.id.tv_danmu_speed);
        sb_font_size.setMax(100);
        if (currentDanmuSpeedLevel == 0) {
            sb_danmu_speed.setProgress(0);
            tv_danmu_speed.setText("超慢");
        } else if (currentDanmuSpeedLevel == 1) {
            sb_danmu_speed.setProgress(25);
            tv_danmu_speed.setText("慢");
        } else if (currentDanmuSpeedLevel == 2) {
            sb_danmu_speed.setProgress(50);
            tv_danmu_speed.setText("正常");
        } else if (currentDanmuSpeedLevel == 3) {
            sb_danmu_speed.setProgress(75);
            tv_danmu_speed.setText("快");
        } else if (currentDanmuSpeedLevel == 4) {
            sb_danmu_speed.setProgress(100);
            tv_danmu_speed.setText("超快");
        }

        sb_danmu_speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (progress >= 0 && progress < 13) {
                    sb_danmu_speed.setProgress(0);
                    currentDanmuSpeedLevel = 0;
                    tv_danmu_speed.setText("超慢");
                } else if (progress >= 13 && progress < 25) {
                    sb_danmu_speed.setProgress(25);
                    currentDanmuSpeedLevel = 1;
                    tv_danmu_speed.setText("慢");
                } else if (progress >= 25 && progress < 38) {
                    sb_danmu_speed.setProgress(25);
                    currentDanmuSpeedLevel = 1;
                    tv_danmu_speed.setText("慢");
                } else if (progress >= 38 && progress < 50) {
                    sb_danmu_speed.setProgress(50);
                    currentDanmuSpeedLevel = 2;
                    tv_danmu_speed.setText("正常");
                } else if (progress >= 50 && progress < 63) {
                    sb_danmu_speed.setProgress(50);
                    currentDanmuSpeedLevel = 2;
                    tv_danmu_speed.setText("正常");
                } else if (progress >= 63 && progress < 75) {
                    sb_danmu_speed.setProgress(75);
                    currentDanmuSpeedLevel = 3;
                    tv_danmu_speed.setText("快");
                } else if (progress >= 75 && progress < 88) {
                    sb_danmu_speed.setProgress(75);
                    currentDanmuSpeedLevel = 3;
                    tv_danmu_speed.setText("快");
                } else if (progress >= 88 && progress <= 100) {
                    sb_danmu_speed.setProgress(100);
                    currentDanmuSpeedLevel = 4;
                    tv_danmu_speed.setText("超快");
                }

                if (onDanmuSet != null) {
                    onDanmuSet.setDanmuSpeed(currentDanmuSpeedLevel);
                }

            }
        });

        final SeekBar sb_display_area = view.findViewById(R.id.sb_display_area);
        final TextView tv_display_area = view.findViewById(R.id.tv_display_area);
        sb_display_area.setProgress(currentDisplayArea);
        sb_display_area.setMax(100);
        tv_display_area.setText(currentDisplayArea + "%");
        sb_display_area.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tv_display_area.setText(i + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (progress<20){
                    sb_display_area.setProgress(20);
                    progress = 20;
                }
                if (onDanmuSet != null) {
                    onDanmuSet.setDisplayArea(progress);
                }
            }
        });

        LinearLayout ll_landscape_danmu_set = view.findViewById(R.id.ll_landscape_danmu_set);
        LinearLayout ll_portrait_danmu_set = view.findViewById(R.id.ll_portrait_danmu_set);
        ImageView iv_close_set_back = view.findViewById(R.id.iv_close_set_back);
        iv_close_set_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics();
        if (isFullScreen){
            ll_landscape_danmu_set.setVisibility(View.VISIBLE);
            ll_portrait_danmu_set.setVisibility(View.GONE);
            lp.width = (int) (d.widthPixels * 0.45);
            lp.height = (int) (d.heightPixels * 1.0);
            dialogWindow.setGravity(Gravity.RIGHT);
        }else {
            ll_landscape_danmu_set.setVisibility(View.GONE);
            ll_portrait_danmu_set.setVisibility(View.VISIBLE);
            lp.width = (int) (d.widthPixels * 1.0);
            lp.height = (int) (d.heightPixels * 0.45);
            dialogWindow.setGravity(Gravity.BOTTOM);
        }

        dialogWindow.setAttributes(lp);


    }


}
