package com.bokecc.vod.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bokecc.vod.R;
import com.bokecc.vod.adapter.SelectVideoAdapter;
import com.bokecc.vod.data.DataUtil;
import com.bokecc.vod.data.HuodeVideoInfo;
import com.bokecc.vod.inter.SelectVideo;

import java.util.ArrayList;
import java.util.List;

public class SelectVideoDialog extends Dialog {
    private Context context;
    private String currentVideoId;
    private SelectVideo selectVideo;
    private ArrayList<HuodeVideoInfo> videoList;

    public SelectVideoDialog(Context context, ArrayList<HuodeVideoInfo> videoList, String currentVideoId, SelectVideo selectVideo) {
        super(context, R.style.SetVideoDialog);
        this.context = context;
        this.currentVideoId = currentVideoId;
        this.selectVideo = selectVideo;
        this.videoList = videoList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_select_video, null);
        setContentView(view);

        ListView lv_select_video = view.findViewById(R.id.lv_select_video);
        List<HuodeVideoInfo> datas = new ArrayList<>();
        for (int i = 0; i < videoList.size(); i++) {
            boolean isSelected = false;
            HuodeVideoInfo videoInfo = videoList.get(i);
            if (videoInfo != null) {
                if (videoInfo.getVideoId().equals(currentVideoId)) {
                    isSelected = true;
                } else {
                    isSelected = false;
                }
                datas.add(new HuodeVideoInfo(videoInfo.getVideoCover(), videoInfo.getVideoTitle(), videoInfo.getVideoId(), isSelected));
            }

        }
        final SelectVideoAdapter selectVideoAdapter = new SelectVideoAdapter(context, datas);
        lv_select_video.setAdapter(selectVideoAdapter);

        lv_select_video.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HuodeVideoInfo item = (HuodeVideoInfo) selectVideoAdapter.getItem(position);
                if (selectVideo != null && item != null) {
                    selectVideo.selectedVideo(item.getVideoTitle(), item.getVideoId(), item.getVideoCover());
                    dismiss();
                }
            }
        });

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics();
        lp.width = (int) (d.widthPixels * 0.35);
        lp.height = (int) (d.heightPixels * 1.0);
        dialogWindow.setAttributes(lp);
        dialogWindow.setGravity(Gravity.RIGHT);
        if (Build.VERSION.SDK_INT > 18) {
            dialogWindow.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

}
