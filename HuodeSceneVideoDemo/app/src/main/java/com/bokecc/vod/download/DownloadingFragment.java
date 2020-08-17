package com.bokecc.vod.download;

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
import android.widget.Button;
import android.widget.ListView;


import com.bokecc.vod.ConfigUtil;
import com.bokecc.vod.R;
import com.bokecc.vod.adapter.DownloadViewAdapter;
import com.bokecc.vod.inter.DeleteFile;
import com.bokecc.vod.upload.UploadController;
import com.bokecc.vod.view.DeleteFileDialog;

import java.io.File;
import java.util.List;

/**
 * 下载中标签页
 *
 * @author 获得场景视频
 */
public class DownloadingFragment extends Fragment implements DownloadController.Observer {

    private FragmentActivity activity;
    private Button btn_all_pause_or_start;
    private ListView lv_download;
    private List<DownloadWrapper> downloadingInfos = DownloadController.downloadingList;
    private DownloadViewAdapter downloadAdapter;
    private DeleteFileDialog deleteFileDialog;
    private boolean isAllPause = false;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = getActivity();
        View view = inflater.inflate(R.layout.fragment_downloading, null);
        lv_download = view.findViewById(R.id.lv_download);
        btn_all_pause_or_start = view.findViewById(R.id.btn_all_pause_or_start);
        initData();

        lv_download.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DownloadController.parseItemClick(position);
                updateListView();
                initAllPause();
            }
        });

        lv_download.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                deleteFileDialog = new DeleteFileDialog(activity, new DeleteFile() {
                    @Override
                    public void deleteFile() {
                        DownloadWrapper wrapper = (DownloadWrapper) downloadAdapter.getItem(position);
                        String title = wrapper.getDownloadInfo().getTitle();
                        String format = wrapper.getDownloadInfo().getFormat();
                        File file = new File(Environment.getExternalStorageDirectory() + "/"+ConfigUtil.DOWNLOAD_PATH, title + format);
                        if (file.exists()) {
                            file.delete();
                        }
                        DownloadController.deleteDownloadingInfo(position);
                        updateListView();
                        initAllPause();
                    }
                });
                deleteFileDialog.show();
                return true;
            }
        });

        initAllPause();

        btn_all_pause_or_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i=0;i<downloadingInfos.size();i++){
                    if (isAllPause){
                        DownloadController.startAllDownload(i);
                    }else {
                        DownloadController.pauseAllDownload(i);
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
        if (DownloadController.getDownloadingCount()>0){
            isAllPause = false;
            btn_all_pause_or_start.setText("全部暂停");
            btn_all_pause_or_start.setVisibility(View.VISIBLE);
        }else if (DownloadController.getPauseAndWaitCount()>0){
            isAllPause = true;
            btn_all_pause_or_start.setText("全部开始");
        }else {
            isAllPause = true;
            btn_all_pause_or_start.setVisibility(View.GONE);
        }
    }


    private void initData() {
        downloadAdapter = new DownloadViewAdapter(activity, downloadingInfos);
        lv_download.setAdapter(downloadAdapter);
    }

    private void updateListView() {
        downloadAdapter.notifyDataSetChanged();
        lv_download.invalidate();
    }

    int downloadingCount = 0;

    @Override
    public void update() {
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                initAllPause();
                updateListView();
                //为防止出现删除提示框展示的时候，新的下载视频完成，导致删除错误的bug，故当有新的下载完成时，取消删除对话框
                int currentDownloadingCount = DownloadController.downloadingList.size();
                if (currentDownloadingCount < downloadingCount) {
                    downloadingCount = currentDownloadingCount;
                    if (deleteFileDialog != null) {
                        deleteFileDialog.dismiss();
                    }
                }
            }
        });
    }

    @Override
    public void onPause() {
        DownloadController.detach(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        downloadingCount = DownloadController.downloadingList.size();
        DownloadController.attach(this);
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}