package com.bokecc.sdk.mobile.demo.play;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.bokecc.sdk.mobile.demo.R;
import com.bokecc.sdk.mobile.demo.adapter.VideoListViewAdapter;

/**
 * 播放列表标签页，用于展示待播放的视频ID
 *
 * @author CC视频
 */
public class PlayFragment extends Fragment {

    private List<Pair<String, Integer>> pairs;

    private VideoListViewAdapter videoListViewAdapter;

    //TODO 待播放视频ID列表，可根据需求自定义
    public static String[] playVideoIds = new String[]{};

    private ListView playListView;
    private Context context;
    private String verificationCode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity().getApplicationContext();
        RelativeLayout playLayout = new RelativeLayout(context);
        playLayout.setBackgroundColor(Color.WHITE);
        LayoutParams playLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        playLayout.setLayoutParams(playLayoutParams);

        playListView = new ListView(context);
        playListView.setDivider(getResources().getDrawable(R.drawable.line));
        playListView.setDividerHeight(2);
        playListView.setPadding(10, 10, 10, 10);
        LayoutParams playListLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        playLayout.addView(playListView, playListLayoutParams);

        // 生成动态数组，加入数据
        pairs = new ArrayList<Pair<String, Integer>>();
        for (int i = 0; i < playVideoIds.length; i++) {
            Pair<String, Integer> pair = new Pair<String, Integer>(playVideoIds[i], R.drawable.play);
            pairs.add(pair);
        }

        videoListViewAdapter = new VideoListViewAdapter(context, pairs);
        playListView.setAdapter(videoListViewAdapter);
        playListView.setOnItemClickListener(onItemClickListener);

        return playLayout;
    }

    OnItemClickListener onItemClickListener = new OnItemClickListener() {

        @SuppressWarnings("unchecked")
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Pair<String, Integer> pair = (Pair<String, Integer>) parent.getItemAtPosition(position);
            showNormalDialog(pair);
            //TODO 使用倍速，则需要添加ijk相关的文件
//			startSpeedAty(pair);
        }
    };

    private void startSpeedAty(final Pair<String, Integer> pair) {
        Intent intent = new Intent(context, SpeedIjkMediaPlayActivity.class);
        intent.putExtra("videoId", pair.first);
        intent.putExtra("verifyCode", verificationCode);
        startActivity(intent);

    }

    private void showNormalDialog(final Pair<String, Integer> pair) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialog dialog = builder.setPositiveButton("广告", new OnClickListener() {

            @Override
            public void onClick(final DialogInterface dialog, int which) {
                Intent intent = new Intent(context, ADMediaPlayActivity.class);
                intent.putExtra("videoId", pair.first);
                intent.putExtra("verifyCode", verificationCode);
                startActivity(intent);
                dialog.dismiss();
            }
        }).setNegativeButton("标准", new OnClickListener() {

            @Override
            public void onClick( DialogInterface dialog, int which) {
                Intent intent = new Intent(context, MediaPlayActivity.class);
                intent.putExtra("videoId", pair.first);
                intent.putExtra("verifyCode", verificationCode);
                startActivity(intent);
                dialog.dismiss();

            }
        }).setTitle("选择播放模式").create();

        dialog.show();
    }

    public void showInputCodeDialog(){
        CustomDialogInputVerifyCode customDialogInputVerifyCode = new CustomDialogInputVerifyCode(getActivity(), new BtnCancelOrSureListener() {
            @Override
            public void btnSure(String verifyCode) {
                verificationCode = verifyCode;
                if (TextUtils.isEmpty(verifyCode)){
                    Toast.makeText(context,"没有输入授权码",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(context,"输入的授权码是："+verificationCode,Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void btnCancel() {

            }
        });
        customDialogInputVerifyCode.show();
    }

    public void showInputVideoIdDialog(){
        CustomDialogInputVideoId customDialogInputVerifyCode = new CustomDialogInputVideoId(getActivity(), new BtnCancelOrSureListener() {
            @Override
            public void btnSure(String videoId) {
                if (TextUtils.isEmpty(videoId)){
                    Toast.makeText(context,"没有输入视频ID",Toast.LENGTH_SHORT).show();
                }else {
                    Pair<String, Integer> pair = new Pair<String, Integer>(videoId, R.drawable.play);
                    pairs.add(pair);
                    videoListViewAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void btnCancel() {

            }
        });
        customDialogInputVerifyCode.show();
    }

}
