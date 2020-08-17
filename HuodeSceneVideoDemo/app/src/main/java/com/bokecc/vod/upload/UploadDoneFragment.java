package com.bokecc.vod.upload;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.bokecc.vod.ConfigUtil;
import com.bokecc.vod.R;
import com.bokecc.vod.adapter.DownloadedViewAdapter;
import com.bokecc.vod.adapter.UploadDoneAdapter;
import com.bokecc.vod.data.DownloadInfo;
import com.bokecc.vod.data.UploadInfo;
import com.bokecc.vod.download.DownloadController;
import com.bokecc.vod.download.DownloadWrapper;
import com.bokecc.vod.inter.DeleteFile;
import com.bokecc.vod.inter.SelectPlayer;
import com.bokecc.vod.play.MediaPlayActivity;
import com.bokecc.vod.play.SpeedPlayActivity;
import com.bokecc.vod.view.DeleteFileDialog;
import com.bokecc.vod.view.SelectPlayerDialog;

import java.io.File;
import java.util.List;

/**
 * 已上传视频
 */
public class UploadDoneFragment extends Fragment implements UploadController.Observer {

    private ListView lv_upload_done;
    private List<UploadWrapper> uploadDoneInfos = UploadController.uploadDoneList;
    private UploadDoneAdapter uploadDoneAdapter;
    private FragmentActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = getActivity();
        View view = inflater.inflate(R.layout.fragment_upload_done, null);
        lv_upload_done = view.findViewById(R.id.lv_upload_done);
        initData();

        lv_upload_done.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                DeleteFileDialog deleteFileDialog = new DeleteFileDialog(activity, new DeleteFile() {
                    @Override
                    public void deleteFile() {
                        UploadWrapper wrapper = (UploadWrapper) uploadDoneAdapter.getItem(position);
                        UploadController.deleteUploadDoneInfo(position);
                        updateView();

                    }
                });
                deleteFileDialog.show();
                return true;
            }
        });
        return view;
    }

    private void initData() {
        uploadDoneAdapter = new UploadDoneAdapter(activity, uploadDoneInfos);
        lv_upload_done.setAdapter(uploadDoneAdapter);
    }

    @Override
    public void update() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateView();
            }
        });
    }

    private void updateView() {
        uploadDoneAdapter.notifyDataSetChanged();
        lv_upload_done.invalidate();
    }

    @Override
    public void onPause() {
        UploadController.detach(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        UploadController.attach(this);
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}