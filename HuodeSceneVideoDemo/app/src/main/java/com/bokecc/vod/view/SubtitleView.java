package com.bokecc.vod.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bokecc.sdk.mobile.play.DWIjkMediaPlayer;
import com.bokecc.sdk.mobile.play.DWMediaPlayer;
import com.bokecc.sdk.mobile.play.OnSubtitleMsgListener;
import com.bokecc.vod.R;

public class SubtitleView extends RelativeLayout {

    private Context mContext;
    private TextView tv_first_subtitle, tv_second_subtitle;
    private Subtitle firstSubtitle, secondSubtitle;
    private int firstBottom, secondBottom, commonBottom, selectedSubtitle = 3;
    private String firstSubName,secondSubName;

    public SubtitleView(Context context) {
        this(context, null);
    }

    public SubtitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        View view = LayoutInflater.from(context).inflate(R.layout.view_subtitle, this, true);
        tv_first_subtitle = view.findViewById(R.id.tv_first_subtitle);
        tv_second_subtitle = view.findViewById(R.id.tv_second_subtitle);
    }

    public void getSubtitlesInfo(DWIjkMediaPlayer player) {
        player.setOnSubtitleMsgListener(new OnSubtitleMsgListener() {
            @Override
            public void onSubtitleMsg(String subtitleName, final int sort, String url, String font, final int size, final String color, final String surroundColor, final double bottom, String code) {
                if (!TextUtils.isEmpty(url)) {
                    firstSubName = subtitleName;
                    firstSubtitle = new Subtitle(new Subtitle.OnSubtitleInitedListener() {
                        @Override
                        public void onInited(Subtitle subtitle) {

                        }
                    });
                    firstSubtitle.initSubtitleResource(url);
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (size > 0) {
                                tv_first_subtitle.setTextSize(size/2);
                            }
                            try {
                                if (!TextUtils.isEmpty(color)) {
                                    String newColor = color;
                                    if (color.contains("0x")) {
                                        newColor = color.replace("0x", "#");
                                    }
                                    tv_first_subtitle.setTextColor(Color.parseColor(newColor));
                                    tv_first_subtitle.setShadowLayer(10F, 5F, 5F, Color.YELLOW);
                                }

                                if (!TextUtils.isEmpty(surroundColor)) {
                                    String newSurroundColor = surroundColor;
                                    if (surroundColor.contains("0x")) {
                                        newSurroundColor = surroundColor.replace("0x", "#");
                                    }
                                    tv_first_subtitle.setShadowLayer(10F, 5F, 5F, Color.parseColor(newSurroundColor));
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

                                if (sort == 2) {
                                    commonBottom = paddingBottom;
                                }
                                firstBottom = paddingBottom;
                                tv_first_subtitle.setPadding(0, 0, 0, paddingBottom);
                            }
                        }
                    });
                }
            }

            @Override
            public void onSecSubtitleMsg(String subtitleName, final int sort, String url, String font, final int size, final String color, final String surroundColor, final double bottom, String code) {
                if (!TextUtils.isEmpty(url)) {
                    secondSubName = subtitleName;
                    secondSubtitle = new Subtitle(new Subtitle.OnSubtitleInitedListener() {
                        @Override
                        public void onInited(Subtitle subtitle) {

                        }
                    });
                    secondSubtitle.initSubtitleResource(url);

                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (size > 0) {
                                tv_second_subtitle.setTextSize(size/2);
                            }
                            try {
                                if (!TextUtils.isEmpty(color)) {
                                    String newColor = color;
                                    if (color.contains("0x")) {
                                        newColor = color.replace("0x", "#");
                                    }
                                    tv_second_subtitle.setTextColor(Color.parseColor(newColor));
                                    tv_second_subtitle.setShadowLayer(10F, 5F, 5F, Color.YELLOW);
                                }

                                if (!TextUtils.isEmpty(surroundColor)) {
                                    String newSurroundColor = surroundColor;
                                    if (surroundColor.contains("0x")) {
                                        newSurroundColor = surroundColor.replace("0x", "#");
                                    }
                                    tv_second_subtitle.setShadowLayer(10F, 5F, 5F, Color.parseColor(newSurroundColor));
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

                                if (sort == 2) {
                                    commonBottom = paddingBottom;
                                }
                                secondBottom = paddingBottom;
                                tv_second_subtitle.setPadding(0, 0, 0, paddingBottom);
                            }
                        }
                    });
                }


            }

            @Override
            public void onDefSubtitle(final int defaultSubtitle) {
                selectedSubtitle = defaultSubtitle;
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (defaultSubtitle == 0) {
                            tv_first_subtitle.setVisibility(View.VISIBLE);
                            tv_second_subtitle.setVisibility(View.GONE);
                            tv_first_subtitle.setPadding(0, 0, 0, commonBottom);
                            tv_second_subtitle.setPadding(0, 0, 0, commonBottom);
                        } else if (defaultSubtitle == 1) {
                            tv_first_subtitle.setVisibility(View.GONE);
                            tv_second_subtitle.setVisibility(View.VISIBLE);
                            tv_first_subtitle.setPadding(0, 0, 0, commonBottom);
                            tv_second_subtitle.setPadding(0, 0, 0, commonBottom);
                        } else {
                            tv_first_subtitle.setVisibility(View.VISIBLE);
                            tv_second_subtitle.setVisibility(View.VISIBLE);
                            tv_first_subtitle.setPadding(0, 0, 0, firstBottom);
                            tv_second_subtitle.setPadding(0, 0, 0, secondBottom);
                        }
                    }
                });
            }
        });
    }

    public void getSubtitlesInfo(DWMediaPlayer player) {
        player.setOnSubtitleMsgListener(new OnSubtitleMsgListener() {
            @Override
            public void onSubtitleMsg(String subtitleName, final int sort, String url, String font, final int size, final String color, final String surroundColor, final double bottom, String code) {
                if (!TextUtils.isEmpty(url)) {
                    firstSubName = subtitleName;
                    firstSubtitle = new Subtitle(new Subtitle.OnSubtitleInitedListener() {
                        @Override
                        public void onInited(Subtitle subtitle) {

                        }
                    });
                    firstSubtitle.initSubtitleResource(url);
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (size > 0) {
                                tv_first_subtitle.setTextSize(size/2);
                            }
                            try {
                                if (!TextUtils.isEmpty(color)) {
                                    String newColor = color;
                                    if (color.contains("0x")) {
                                        newColor = color.replace("0x", "#");
                                    }
                                    tv_first_subtitle.setTextColor(Color.parseColor(newColor));
                                    tv_first_subtitle.setShadowLayer(10F, 5F, 5F, Color.YELLOW);
                                }

                                if (!TextUtils.isEmpty(surroundColor)) {
                                    String newSurroundColor = surroundColor;
                                    if (surroundColor.contains("0x")) {
                                        newSurroundColor = surroundColor.replace("0x", "#");
                                    }
                                    tv_first_subtitle.setShadowLayer(10F, 5F, 5F, Color.parseColor(newSurroundColor));
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

                                if (sort == 2) {
                                    commonBottom = paddingBottom;
                                }
                                firstBottom = paddingBottom;
                                tv_first_subtitle.setPadding(0, 0, 0, paddingBottom);
                            }
                        }
                    });
                }
            }

            @Override
            public void onSecSubtitleMsg(String subtitleName, final int sort, String url, String font, final int size, final String color, final String surroundColor, final double bottom, String code) {
                if (!TextUtils.isEmpty(url)) {
                    secondSubName = subtitleName;
                    secondSubtitle = new Subtitle(new Subtitle.OnSubtitleInitedListener() {
                        @Override
                        public void onInited(Subtitle subtitle) {

                        }
                    });
                    secondSubtitle.initSubtitleResource(url);

                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (size > 0) {
                                tv_second_subtitle.setTextSize(size/2);
                            }
                            try {
                                if (!TextUtils.isEmpty(color)) {
                                    String newColor = color;
                                    if (color.contains("0x")) {
                                        newColor = color.replace("0x", "#");
                                    }
                                    tv_second_subtitle.setTextColor(Color.parseColor(newColor));
                                    tv_second_subtitle.setShadowLayer(10F, 5F, 5F, Color.YELLOW);
                                }

                                if (!TextUtils.isEmpty(surroundColor)) {
                                    String newSurroundColor = surroundColor;
                                    if (surroundColor.contains("0x")) {
                                        newSurroundColor = surroundColor.replace("0x", "#");
                                    }
                                    tv_second_subtitle.setShadowLayer(10F, 5F, 5F, Color.parseColor(newSurroundColor));
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

                                if (sort == 2) {
                                    commonBottom = paddingBottom;
                                }
                                secondBottom = paddingBottom;
                                tv_second_subtitle.setPadding(0, 0, 0, paddingBottom);
                            }
                        }
                    });
                }


            }

            @Override
            public void onDefSubtitle(final int defaultSubtitle) {
                selectedSubtitle = defaultSubtitle;
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (defaultSubtitle == 0) {
                            tv_first_subtitle.setVisibility(View.VISIBLE);
                            tv_second_subtitle.setVisibility(View.GONE);
                            tv_first_subtitle.setPadding(0, 0, 0, commonBottom);
                            tv_second_subtitle.setPadding(0, 0, 0, commonBottom);
                        } else if (defaultSubtitle == 1) {
                            tv_first_subtitle.setVisibility(View.GONE);
                            tv_second_subtitle.setVisibility(View.VISIBLE);
                            tv_first_subtitle.setPadding(0, 0, 0, commonBottom);
                            tv_second_subtitle.setPadding(0, 0, 0, commonBottom);
                        } else {
                            tv_first_subtitle.setVisibility(View.VISIBLE);
                            tv_second_subtitle.setVisibility(View.VISIBLE);
                            tv_first_subtitle.setPadding(0, 0, 0, firstBottom);
                            tv_second_subtitle.setPadding(0, 0, 0, secondBottom);
                        }
                    }
                });
            }
        });
    }

    //更新字幕
    public void refreshSubTitle(long currentPosition) {
        if (firstSubtitle != null) {
            tv_first_subtitle.setText(firstSubtitle.getSubtitleByTime(currentPosition));
        } else {
            tv_first_subtitle.setVisibility(INVISIBLE);
        }

        if (secondSubtitle != null) {
            tv_second_subtitle.setText(secondSubtitle.getSubtitleByTime(currentPosition));
        } else {
            tv_second_subtitle.setVisibility(INVISIBLE);
        }
    }

    //重置字幕
    public void resetSubtitle() {
        firstSubtitle = null;
        secondSubtitle = null;
        selectedSubtitle = 3;
        firstSubName = null;
        secondSubName = null;
    }

    //获得第一种字幕名字
    public String getFirstSubName(){
        return firstSubName;
    }

    //获得第二种字幕名字
    public String getSecondSubName(){
        return secondSubName;
    }

    //获得当前选中的字幕
    public int getSelectedSubtitle(){
        return selectedSubtitle;
    }

    //控制字幕显示
    public void setSubtitle(int selectedSub){
        selectedSubtitle = selectedSub;
        if (selectedSub==0){
            tv_first_subtitle.setVisibility(VISIBLE);
            tv_second_subtitle.setVisibility(GONE);
            tv_first_subtitle.setPadding(0, 0, 0, commonBottom);
        }else if (selectedSub==1){
            tv_first_subtitle.setVisibility(GONE);
            tv_second_subtitle.setVisibility(VISIBLE);
            tv_second_subtitle.setPadding(0, 0, 0, commonBottom);
        }else if (selectedSub==2){
            tv_first_subtitle.setVisibility(View.VISIBLE);
            tv_second_subtitle.setVisibility(View.VISIBLE);
            tv_first_subtitle.setPadding(0, 0, 0, firstBottom);
            tv_second_subtitle.setPadding(0, 0, 0, secondBottom);
        }else if (selectedSub==3){
            tv_first_subtitle.setVisibility(View.GONE);
            tv_second_subtitle.setVisibility(View.GONE);
        }
    }

}
