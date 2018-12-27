package com.bokecc.sdk.mobile.demo.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.sdk.mobile.demo.R;
import com.bokecc.sdk.mobile.demo.util.NetUtils;
import com.bokecc.sdk.mobile.play.PlayInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomDialogCheckNetwork extends Dialog {
    private Activity context;
    private String videoId,videoServerUrl,pingVideoUrl;
    private String netType;
    private PlayInfo playInfo;
    private ClipboardManager cmb;
    private final String ccUrl = "http://p.bokecc.com";
    private int successTimesCC,failTimesCC,totalRttTime;
    private int successTimesVideoServer,failTimesVideoServer,totalRttTimeVideoServer;
    private int firstCcAvgRTT,secondCcAvgRTT,thirdCcAvgRTT,fourthCcAvgRTT;
    private int firstVideoServerAvgRTT,secondVideoServerAvgRTT,thirdVideoServerAvgRTT,fourthVideoServerAvgRTT;
    private List<Integer> rtt = new ArrayList<>();
    private List<Integer> rttVideoServer = new ArrayList<>();
    private boolean isPingCcOk = false, isPingVideoServerOk = false;
    private LinearLayout ll_ping_video_server,ll_ping_cc;
    private ProgressBar pb_ping_video_server,pb_ping_cc;
    private TextView tv_network,tv_first_ping_bokecc,tv_second_ping_bokecc,tv_third_ping_bokecc,
            tv_fourth_ping_bokecc,tv_result_ping_bokecc,tv_abstract_ping_bokecc,tv_video_title,
            tv_play_url,tv_ping_video_server,tv_first_ping_video_server,tv_second_ping_video_server,
            tv_third_ping_video_server,tv_fourth_ping_video_server,tv_result_ping_video_server,
            tv_abstract_ping_video_server,tv_local_ip,tv_videoid;

    public CustomDialogCheckNetwork(@NonNull Activity context, String videoId, PlayInfo playInfo) {
        super(context, R.style.CustomDialogInputVerifyCode);
        this.context = context;
        this.videoId = videoId;
        this.playInfo = playInfo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_check_network, null);
        setContentView(view);
        videoServerUrl = playInfo.getPlayUrl();
        ImageView iv_back_check_network = view.findViewById(R.id.iv_back_check_network);
        tv_videoid = view.findViewById(R.id.tv_videoid);
        tv_network = view.findViewById(R.id.tv_network);
        tv_first_ping_bokecc = view.findViewById(R.id.tv_first_ping_bokecc);
        tv_second_ping_bokecc = view.findViewById(R.id.tv_second_ping_bokecc);
        tv_third_ping_bokecc = view.findViewById(R.id.tv_third_ping_bokecc);
        tv_fourth_ping_bokecc = view.findViewById(R.id.tv_fourth_ping_bokecc);
        tv_result_ping_bokecc = view.findViewById(R.id.tv_result_ping_bokecc);
        tv_abstract_ping_bokecc = view.findViewById(R.id.tv_abstract_ping_bokecc);
        tv_video_title = view.findViewById(R.id.tv_video_title);
        tv_play_url = view.findViewById(R.id.tv_play_url);
        tv_ping_video_server = view.findViewById(R.id.tv_ping_video_server);
        tv_first_ping_video_server = view.findViewById(R.id.tv_first_ping_video_server);
        tv_second_ping_video_server = view.findViewById(R.id.tv_second_ping_video_server);
        tv_third_ping_video_server = view.findViewById(R.id.tv_third_ping_video_server);
        tv_fourth_ping_video_server = view.findViewById(R.id.tv_fourth_ping_video_server);
        tv_result_ping_video_server = view.findViewById(R.id.tv_result_ping_video_server);
        tv_abstract_ping_video_server = view.findViewById(R.id.tv_abstract_ping_video_server);
        ll_ping_video_server = view.findViewById(R.id.ll_ping_video_server);
        ll_ping_cc = view.findViewById(R.id.ll_ping_cc);
        pb_ping_video_server = view.findViewById(R.id.pb_ping_video_server);
        pb_ping_cc = view.findViewById(R.id.pb_ping_cc);
        Button btn_copy_check_info = view.findViewById(R.id.btn_copy_check_info);
        Button btn_recheck = view.findViewById(R.id.btn_recheck);

        tv_local_ip = view.findViewById(R.id.tv_local_ip);
        tv_videoid.setText(videoId);

        iv_back_check_network.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics();
        lp.width = (int) (d.widthPixels * 1.0);
        lp.height = (int) (d.heightPixels * 1.0);
        dialogWindow.setAttributes(lp);


        if (playInfo!=null){
            tv_video_title.setText(playInfo.getTitle());
            tv_play_url.setText(playInfo.getPlayUrl());
            if (!TextUtils.isEmpty(videoServerUrl)){
                if (videoServerUrl.startsWith("https")){
                    pingVideoUrl = videoServerUrl.substring(8, videoServerUrl.length());
                }else if (videoServerUrl.startsWith("http")){
                    pingVideoUrl = videoServerUrl.substring(7, videoServerUrl.length());
                }
                tv_ping_video_server.setText("ping "+pingVideoUrl);
            }

        }
        checkNetInfo();

        btn_copy_check_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPingCcOk && isPingVideoServerOk){
                    String checkInfo = "视频ID：" + getText(tv_videoid) + "\n" + "当前网络：" + getText(tv_network)+ "\n" +"本地出口IP："+getText(tv_local_ip)
                            + "\n" +"ping p.bokecc.com"+"\n" +getText(tv_first_ping_bokecc)+"\n" +getText(tv_second_ping_bokecc)+"\n"+getText(tv_third_ping_bokecc)
                            +"\n"+getText(tv_fourth_ping_bokecc)+"\n"+getText(tv_result_ping_bokecc)+"\n"+getText(tv_abstract_ping_bokecc)+"\n"+"视频信息："+getText(tv_video_title)
                            +"\n"+"服务地址："+getText(tv_play_url)+"\n"+"ping "+getText(tv_ping_video_server)+"\n"+getText(tv_first_ping_video_server)
                            +"\n"+getText(tv_second_ping_video_server)+"\n"+getText(tv_third_ping_video_server)+"\n"+getText(tv_fourth_ping_video_server)
                            +"\n"+getText(tv_result_ping_video_server)+"\n"+getText(tv_abstract_ping_video_server);
                    cmb.setText(checkInfo);
                    Toast.makeText(context,"复制剪切信息成功",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(context,"请稍候，本次网络检测还没结束",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_recheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPingCcOk && isPingVideoServerOk){
                    isPingCcOk = false;
                    isPingVideoServerOk = false;
                    successTimesCC = 0;
                    failTimesCC = 0;
                    totalRttTime = 0;
                    successTimesVideoServer = 0;
                    failTimesVideoServer = 0;
                    totalRttTimeVideoServer = 0;
                    rtt.clear();
                    rttVideoServer.clear();
                    ll_ping_video_server.setVisibility(View.GONE);
                    pb_ping_video_server.setVisibility(View.VISIBLE);
                    pb_ping_cc.setVisibility(View.VISIBLE);
                    ll_ping_cc.setVisibility(View.GONE);
                    checkNetInfo();
                }else {
                    Toast.makeText(context,"请稍候，本次网络检测还没结束",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private String getText(TextView textView){
        return textView.getText().toString();
    }
    private void checkNetInfo() {
        //获取当前网络状态
        getNetworkType(tv_network);
        //获取本地出口IP
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String localIPAddress = NetUtils.getOutNetIP(context, 0);

                //获取第一次ping值
                final String ipFromUrl = NetUtils.getIPFromUrl(ccUrl);
                firstCcAvgRTT = NetUtils.getAvgRTT(ccUrl);
                secondCcAvgRTT = NetUtils.getAvgRTT(ccUrl);
                thirdCcAvgRTT = NetUtils.getAvgRTT(ccUrl);
                fourthCcAvgRTT = NetUtils.getAvgRTT(ccUrl);
                if (firstCcAvgRTT>0){
                    successTimesCC = successTimesCC + 1;
                    rtt.add(firstCcAvgRTT);
                    totalRttTime = totalRttTime + firstCcAvgRTT;
                }else {
                    failTimesCC = failTimesCC + 1;
                }

                if (secondCcAvgRTT>0){
                    successTimesCC = successTimesCC + 1;
                    rtt.add(secondCcAvgRTT);
                    totalRttTime = totalRttTime + secondCcAvgRTT;
                }else {
                    failTimesCC = failTimesCC + 1;
                }

                if (thirdCcAvgRTT>0){
                    successTimesCC = successTimesCC + 1;
                    rtt.add(thirdCcAvgRTT);
                    totalRttTime = totalRttTime + thirdCcAvgRTT;
                }else {
                    failTimesCC = failTimesCC + 1;
                }

                if (fourthCcAvgRTT>0){
                    successTimesCC = successTimesCC + 1;
                    rtt.add(fourthCcAvgRTT);
                    totalRttTime = totalRttTime + fourthCcAvgRTT;
                }else {
                    failTimesCC = failTimesCC + 1;
                }



                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_local_ip.setText(localIPAddress);
                        pb_ping_cc.setVisibility(View.GONE);
                        ll_ping_cc.setVisibility(View.VISIBLE);
                        tv_first_ping_bokecc.setText("来自 " + ipFromUrl + " 的回复：字节=32  时间=" + firstCcAvgRTT + "ms");
                        tv_second_ping_bokecc.setText("来自 " + ipFromUrl + " 的回复：字节=32  时间=" + secondCcAvgRTT + "ms");
                        tv_third_ping_bokecc.setText("来自 " + ipFromUrl + " 的回复：字节=32  时间=" + thirdCcAvgRTT + "ms");
                        tv_fourth_ping_bokecc.setText("来自 " + ipFromUrl + " 的回复：字节=32  时间=" + fourthCcAvgRTT + "ms");
                        int failRate = failTimesCC * 100 / 4;
                        tv_result_ping_bokecc.setText("数据包： 已发送 = 4， 已接收 = "+successTimesCC+"，丢失 = "+failTimesCC+"（"+failRate+"% 丢失）");
                        Integer max = Collections.max(rtt);
                        Integer min = Collections.min(rtt);
                        if (max==null){
                            max = 0;
                        }

                        if (min==null){
                            min = 0;
                        }
                        int avRttTime = totalRttTime / successTimesCC;

                        tv_abstract_ping_bokecc.setText("最短 = "+min+"ms， 最长 = "+max+"ms， 平均 = "+avRttTime+"ms");
                        isPingCcOk = true;
                    }
                });
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                //获取第一次ping值
                final String videoServerIp = NetUtils.getIPFromUrl(videoServerUrl);
                firstVideoServerAvgRTT = NetUtils.getAvgRTT(videoServerUrl);
                secondVideoServerAvgRTT = NetUtils.getAvgRTT(videoServerUrl);
                thirdVideoServerAvgRTT = NetUtils.getAvgRTT(videoServerUrl);
                fourthVideoServerAvgRTT = NetUtils.getAvgRTT(videoServerUrl);

                if (firstVideoServerAvgRTT>0){
                    successTimesVideoServer = successTimesVideoServer + 1;
                    rttVideoServer.add(firstVideoServerAvgRTT);
                    totalRttTimeVideoServer = totalRttTimeVideoServer + firstVideoServerAvgRTT;
                }else {
                    failTimesVideoServer = failTimesVideoServer + 1;
                }

                if (secondVideoServerAvgRTT>0){
                    successTimesVideoServer = successTimesVideoServer + 1;
                    rttVideoServer.add(secondVideoServerAvgRTT);
                    totalRttTimeVideoServer = totalRttTimeVideoServer + secondVideoServerAvgRTT;
                }else {
                    failTimesVideoServer = failTimesVideoServer + 1;
                }

                if (thirdVideoServerAvgRTT>0){
                    successTimesVideoServer = successTimesVideoServer + 1;
                    rttVideoServer.add(thirdVideoServerAvgRTT);
                    totalRttTimeVideoServer = totalRttTimeVideoServer + thirdVideoServerAvgRTT;
                }else {
                    failTimesVideoServer = failTimesVideoServer + 1;
                }

                if (fourthVideoServerAvgRTT>0){
                    successTimesVideoServer = successTimesVideoServer + 1;
                    rttVideoServer.add(fourthVideoServerAvgRTT);
                    totalRttTimeVideoServer = totalRttTimeVideoServer + fourthVideoServerAvgRTT;
                }else {
                    failTimesVideoServer = failTimesVideoServer + 1;
                }

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ll_ping_video_server.setVisibility(View.VISIBLE);
                        pb_ping_video_server.setVisibility(View.GONE);
                        tv_first_ping_video_server.setText("来自 " + videoServerIp + " 的回复：字节=32  时间=" + firstVideoServerAvgRTT + "ms");
                        tv_second_ping_video_server.setText("来自 " + videoServerIp + " 的回复：字节=32  时间=" + secondVideoServerAvgRTT + "ms");
                        tv_third_ping_video_server.setText("来自 " + videoServerIp + " 的回复：字节=32  时间=" + thirdVideoServerAvgRTT + "ms");
                        tv_fourth_ping_video_server.setText("来自 " + videoServerIp + " 的回复：字节=32  时间=" + fourthVideoServerAvgRTT + "ms");
                        int failRate = failTimesVideoServer * 100 / 4;
                        tv_result_ping_video_server.setText("数据包： 已发送 = 4， 已接收 = "+successTimesVideoServer+"，丢失 = "+failTimesVideoServer+"（"+failRate+"% 丢失）");

                        Integer max = Collections.max(rttVideoServer);
                        Integer min = Collections.min(rttVideoServer);
                        if (max==null){
                            max = 0;
                        }

                        if (min==null){
                            min = 0;
                        }
                        int avRttTime = totalRttTimeVideoServer / successTimesVideoServer;

                        tv_abstract_ping_video_server.setText("最短 = "+min+"ms， 最长 = "+max+"ms， 平均 = "+avRttTime+"ms");
                        isPingVideoServerOk = true;
                    }
                });
            }
        }).start();
    }

    private void getNetworkType(TextView tv_network) {
        int networkState = NetUtils.getNetworkState(context);
        if (networkState == 0) {
            netType = "没有网络连接";
        } else if (networkState == 1) {
            netType = "Wifi";
        } else if (networkState == 2) {
            netType = "2G";
        } else if (networkState == 3) {
            netType = "3G";
        } else if (networkState == 4) {
            netType = "4G";
        } else if (networkState == 5) {
            netType = "手机流量";
        }

        tv_network.setText(netType);
    }
}
