package com.bokecc.vod.upload;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.bokecc.vod.R;
import com.bokecc.vod.adapter.UploadingAdapter;
import com.bokecc.vod.inter.DeleteFile;
import com.bokecc.vod.view.DeleteFileDialog;

import java.util.List;

/**
 * 正在上传的视频
 */
public class UploadingFragment extends Fragment implements UploadController.Observer {

    private FragmentActivity activity;
    private Button btn_all_pause_or_start;
    private ListView lv_upload;
    private List<UploadWrapper> uploadingInfos = UploadController.uploadingList;
    private UploadingAdapter newUploadingAdapter;
    private DeleteFileDialog deleteFileDialog;
    private boolean isAllPause = false;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = getActivity();
        View view = inflater.inflate(R.layout.fragment_uploading, null);
        lv_upload = view.findViewById(R.id.lv_upload);
        btn_all_pause_or_start = view.findViewById(R.id.btn_all_pause_or_start);
        initData();

        lv_upload.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UploadController.parseItemClick(position);
                updateListView();
                initAllPause();
            }
        });

        lv_upload.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                deleteFileDialog = new DeleteFileDialog(activity, new DeleteFile() {
                    @Override
                    public void deleteFile() {
                        UploadController.deleteUploadingInfo(position);
                        updateListView();
                        initAllPause();
                    }
                });
                deleteFileDialog.show();
                return true;
            }
        });

        btn_all_pause_or_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i< uploadingInfos.size(); i++){
                    if (isAllPause){
                        UploadController.startAllUpload(i);
                    }else {
                        UploadController.pauseAllUpload(i);
                    }
                }
                updateListView();
                if (isAllPause){
                    isAllPause = false;
                    btn_all_pause_or_start.setText("全部暂停");
                }else {
                    isAllPause = true;
                    btn_all_pause_or_start.setText("全部开始");
                }
            }
        });
        return view;
    }

    private void initAllPause() {
        if (UploadController.getUploadingCount()>0){
            isAllPause = false;
            btn_all_pause_or_start.setText("全部暂停");
            btn_all_pause_or_start.setVisibility(View.VISIBLE);
        }else if (UploadController.getPauseAndWaitCount()>0){
            isAllPause = true;
            btn_all_pause_or_start.setText("全部开始");
        }else {
            isAllPause = true;
            btn_all_pause_or_start.setVisibility(View.GONE);
        }
    }


    private void initData() {
        newUploadingAdapter = new UploadingAdapter(activity, uploadingInfos);
        lv_upload.setAdapter(newUploadingAdapter);
    }

    private void updateListView() {
        newUploadingAdapter.notifyDataSetChanged();
        lv_upload.invalidate();
    }

    int uploadingCount = 0;

    @Override
    public void update() {
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                initAllPause();
                updateListView();
                //为防止出现删除提示框展示的时候，新的上传视频完成，导致删除错误的bug，故当有新的上传完成时，取消删除对话框
                int currentDownloadingCount = UploadController.uploadingList.size();
                if (currentDownloadingCount < uploadingCount) {
                    uploadingCount = currentDownloadingCount;
                    if (deleteFileDialog != null) {
                        deleteFileDialog.dismiss();
                    }
                }
            }
        });
    }

    @Override
    public void onPause() {
        UploadController.detach(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        uploadingCount = UploadController.uploadingList.size();
        UploadController.attach(this);
        initAllPause();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}