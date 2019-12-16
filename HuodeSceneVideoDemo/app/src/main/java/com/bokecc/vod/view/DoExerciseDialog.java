package com.bokecc.vod.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bokecc.sdk.mobile.play.InfoCollector;
import com.bokecc.sdk.mobile.play.OnExerciseStatisticsListener;
import com.bokecc.vod.ConfigUtil;
import com.bokecc.vod.R;
import com.bokecc.vod.adapter.ExerciseAdapter;
import com.bokecc.vod.adapter.StatisticResultAdapter;
import com.bokecc.vod.data.ExeQuestion;
import com.bokecc.vod.data.Exercise;
import com.bokecc.vod.data.ExerciseStatistic;
import com.bokecc.vod.inter.DoExerciseResult;
import com.bokecc.vod.inter.ExercisesContinuePlay;
import com.bokecc.vod.utils.MultiUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DoExerciseDialog extends Dialog {

    private Context context;
    private Exercise exercise;
    private String videoId;
    JSONArray jsonArray = new JSONArray();
    private InfoCollector infoCollector;
    private List<ExerciseStatistic> statistics;
    private int answerRightCount = 0;
    private ExercisesContinuePlay exercisesContinuePlay;
    private LinearLayout ll_statistic;
    private ViewPager vp_exe;
    private ListView lv_statistics;
    private TextView tv_total_question_count;
    private TextView tv_answer_right_count;
    private PopupWindow popupWindow;
    private int lastClickPos = 0, currentPagePos = 0, maxPos;
    private TextView tv_rate;
    private StatisticResultAdapter statisticResultAdapter;
    private float downX, upX, vpDownX, vpUpx;
    private boolean isShowStatisticResult = false, isGetVpDownX = false;

    public DoExerciseDialog(Context context, Exercise exercise, String videoId, ExercisesContinuePlay exercisesContinuePlay) {
        super(context, R.style.DeleteFileDialog);
        this.context = context;
        this.exercise = exercise;
        this.videoId = videoId;
        this.exercisesContinuePlay = exercisesContinuePlay;
        infoCollector = new InfoCollector();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_do_exercise, null);
        setContentView(view);
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics();
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            lp.width = (int) (d.widthPixels * 0.9);
            lp.height = (int) (d.heightPixels * 0.9);
        } else {
            lp.width = (int) (d.heightPixels * 0.9);
            lp.height = (int) (d.widthPixels * 0.9);
        }

        dialogWindow.setAttributes(lp);
        setCanceledOnTouchOutside(false);
        setCancelable(false);

        vp_exe = view.findViewById(R.id.vp_exe);
        ll_statistic = view.findViewById(R.id.ll_statistic);
        tv_answer_right_count = view.findViewById(R.id.tv_answer_right_count);
        tv_total_question_count = view.findViewById(R.id.tv_total_question_count);
        Button btn_continue_play = view.findViewById(R.id.btn_continue_play);
        lv_statistics = view.findViewById(R.id.lv_statistics);
        final ProgressBar pb_do_exercise = view.findViewById(R.id.pb_do_exercise);

        ExerciseAdapter exerciseAdapter = new ExerciseAdapter(context, exercise.getExeQuestions());
        vp_exe.setAdapter(exerciseAdapter);
        maxPos = exercise.getExeQuestions().size() - 1;
        vp_exe.setOffscreenPageLimit(exercise.getExeQuestions().size() - 1);
        pb_do_exercise.setMax(exercise.getExeQuestions().size());
        pb_do_exercise.setProgress(1);

        vp_exe.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int i) {
                pb_do_exercise.setProgress((i + 1));
                currentPagePos = i;
                if (i > 0) {
                    String s = jsonArray.toString();
                    ExeQuestion exeQuestion = exercise.getExeQuestions().get(i - 1);
                    String questionId = exeQuestion.getId() + "";
                    if (!s.contains(questionId)) {
                        vp_exe.setCurrentItem(i - 1);
                    }
                }

                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        exerciseAdapter.setDoExerciseResult(new DoExerciseResult() {
            @Override
            public void answerResult(int questionId, int isRight) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("questionId", questionId);
                    jsonObject.put("isRight", isRight);
                    jsonArray.put(jsonObject);
                    if (jsonArray.length() == exercise.getExeQuestions().size()) {
                        String questionMes = jsonArray.toString();
                        int exerciseId = exercise.getId();
                        infoCollector.reportExerciseInfo(exerciseId + "", questionMes, videoId, ConfigUtil.USERID);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        infoCollector.setOnExerciseStatisticsListener(new OnExerciseStatisticsListener() {
            @Override
            public void onSuccess(JSONArray exerciseStatistics) {
                statistics = new ArrayList<>();
                for (int i = 0; i < exerciseStatistics.length(); i++) {
                    try {
                        ExerciseStatistic exerciseStatistic = new ExerciseStatistic(exerciseStatistics.getJSONObject(i));
                        for (int j = 0; j < jsonArray.length(); j++) {
                            JSONObject jsonObject = (JSONObject) jsonArray.get(j);
                            if (jsonObject != null) {
                                int questionId = jsonObject.getInt("questionId");
                                int isRight = jsonObject.getInt("isRight");
                                if (questionId == exerciseStatistic.getQuestionId()) {
                                    if (isRight == 1) {
                                        exerciseStatistic.setAnswerRight(true);
                                    } else if (isRight == 0) {
                                        exerciseStatistic.setAnswerRight(false);
                                    }
                                }
                            }

                        }
                        statistics.add(exerciseStatistic);
                    } catch (JSONException e) {

                    }
                }
                for (ExerciseStatistic exerciseSta : statistics) {
                    if (exerciseSta.isAnswerRight()) {
                        answerRightCount++;
                    }
                }
                handler.sendEmptyMessageDelayed(1, 1000);
            }

            @Override
            public void onError(String errorMsg) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismiss();
                    }
                });
            }
        });

        btn_continue_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                exercisesContinuePlay.continuePlay();
            }
        });

        lv_statistics.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ExerciseStatistic item = (ExerciseStatistic) statisticResultAdapter.getItem(position);
                int accuracy = item.getAccuracy();
                if (lastClickPos != position) {
                    if (popupWindow != null && popupWindow.isShowing()) {
                        popupWindow.dismiss();
                    }
                } else {
                    if (popupWindow != null && popupWindow.isShowing()) {
                        popupWindow.dismiss();
                        return;
                    }
                }
                lastClickPos = position;

                if (popupWindow == null) {
                    View showRateView = LayoutInflater.from(context).inflate(R.layout.view_show_right_or_error_rate, null);
                    tv_rate = showRateView.findViewById(R.id.tv_rate);
                    popupWindow = new PopupWindow(showRateView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                    popupWindow.setTouchable(false);
                    popupWindow.showAsDropDown(view, (int) (view.getWidth() * 0.65), (int) -(view.getHeight() * 2.1));
                } else {
                    popupWindow.showAsDropDown(view, (int) (view.getWidth() * 0.65), (int) -(view.getHeight() * 2.1));
                }
                if (item.isAnswerRight()) {
                    tv_rate.setText("居然有" + accuracy + "%的人答对了");
                } else {
                    tv_rate.setText("居然有" + (100 - accuracy) + "%的人答错了");
                }

            }
        });

        ll_statistic.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getX();
                        break;

                    case MotionEvent.ACTION_UP:
                        upX = event.getX();
                        float difX = upX - downX;
                        if (difX > 100) {
                            ll_statistic.setVisibility(View.GONE);
                            vp_exe.setVisibility(View.VISIBLE);
                            if (popupWindow != null && popupWindow.isShowing()) {
                                popupWindow.dismiss();
                            }
                        }
                        break;
                }
                return true;
            }
        });

        lv_statistics.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getX();
                        break;

                    case MotionEvent.ACTION_UP:
                        upX = event.getX();
                        float difX = upX - downX;
                        if (difX > 100) {
                            ll_statistic.setVisibility(View.GONE);
                            vp_exe.setVisibility(View.VISIBLE);
                            if (popupWindow != null && popupWindow.isShowing()) {
                                popupWindow.dismiss();
                            }
                        }
                        break;
                }
                return false;
            }
        });

        vp_exe.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        if (!isGetVpDownX) {
                            vpDownX = event.getX();
                        }
                        isGetVpDownX = true;
                        break;

                    case MotionEvent.ACTION_UP:
                        vpUpx = event.getX();
                        isGetVpDownX = false;
                        float difX = vpDownX - vpUpx;
                        if (difX > 100 && isShowStatisticResult && (currentPagePos == maxPos)) {
                            ll_statistic.setVisibility(View.VISIBLE);
                            vp_exe.setVisibility(View.GONE);
                        }
                        break;
                }
                return false;
            }
        });

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    ll_statistic.setVisibility(View.VISIBLE);
                    vp_exe.setVisibility(View.GONE);
                    statisticResultAdapter = new StatisticResultAdapter(context, statistics);
                    lv_statistics.setAdapter(statisticResultAdapter);
                    tv_answer_right_count.setText(answerRightCount + "");
                    tv_total_question_count.setText("题，共" + statistics.size() + "题");
                    isShowStatisticResult = true;
                    break;
            }
        }
    };

}
