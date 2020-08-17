package com.bokecc.vod.download;

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
import com.bokecc.vod.R;
import com.bokecc.vod.adapter.DownloadedViewAdapter;
import com.bokecc.vod.ConfigUtil;
import com.bokecc.vod.data.DownloadInfo;
import com.bokecc.vod.inter.DeleteFile;
import com.bokecc.vod.inter.SelectPlayer;
import com.bokecc.vod.play.MediaPlayActivity;
import com.bokecc.vod.play.SpeedPlayActivity;
import com.bokecc.vod.play.VrPlayActivity;
import com.bokecc.vod.view.DeleteFileDialog;
import com.bokecc.vod.view.SelectPlayerDialog;

import java.io.File;
import java.util.List;

/**
 * 已下载视频
 */
public class DownloadedFragment extends Fragment implements DownloadController.Observer {

    private ListView lv_downloaded;
    private List<DownloadWrapper> downloadedInfos = DownloadController.downloadedList;
    private DownloadedViewAdapter videoListViewAdapter;
    private FragmentActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = getActivity();
        View view = inflater.inflate(R.layout.fragment_download, null);
        lv_downloaded = view.findViewById(R.id.lv_download);
        initData();

        lv_downloaded.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final DownloadWrapper wrapper = (DownloadWrapper) videoListViewAdapter.getItem(position);
                final DownloadInfo downloadInfo = wrapper.getDownloadInfo();
                if (downloadInfo==null){
                    return;
                }
                SelectPlayerDialog selectPlayerDialog = new SelectPlayerDialog(activity, new SelectPlayer() {
                    @Override
                    public void selectDWIjkMediaPlayer() {
                        Intent intent = new Intent(activity, SpeedPlayActivity.class);
                        intent.putExtra("videoId", downloadInfo.getVideoId());
                        intent.putExtra("isLocalPlay", true);
                        intent.putExtra("videoTitle", downloadInfo.getTitle());
                        intent.putExtra("format", downloadInfo.getFormat());
                        startActivity(intent);
                    }

                    @Override
                    public void selectDWMediaPlayer() {
                        Intent intent = new Intent(activity, MediaPlayActivity.class);
                        intent.putExtra("videoId", downloadInfo.getVideoId());
                        intent.putExtra("isLocalPlay", true);
                        intent.putExtra("videoTitle", downloadInfo.getTitle());
                        intent.putExtra("format", downloadInfo.getFormat());
                        startActivity(intent);
                    }

                    @Override
                    public void selectVrPlay() {
                        Intent intent = new Intent(activity, VrPlayActivity.class);
                        intent.putExtra("videoId", downloadInfo.getVideoId());
                        intent.putExtra("isLocalPlay", true);
                        intent.putExtra("videoTitle", downloadInfo.getTitle());
                        intent.putExtra("format", downloadInfo.getFormat());
                        intent.putExtra("isVr", true);
                        startActivity(intent);
                    }
                });
                selectPlayerDialog.show();

            }
        });

        lv_downloaded.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                DeleteFileDialog deleteFileDialog = new DeleteFileDialog(activity, new DeleteFile() {
                    @Override
                    public void deleteFile() {
                        DownloadWrapper wrapper = (DownloadWrapper) videoListViewAdapter.getItem(position);
                        DownloadController.deleteDownloadedInfo(position);
                        File file = new File(Environment.getExternalStorageDirectory()+"/"+ConfigUtil.DOWNLOAD_PATH, wrapper.getDownloadInfo().getTitle()+wrapper.getDownloadInfo().getFormat());
                        if(file.exists()){
                            Log.i("dwdemo", "删除已下载视频成功：" + file.getAbsolutePath());
                            file.delete();
                        }
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
        videoListViewAdapter = new DownloadedViewAdapter(activity, downloadedInfos);
        lv_downloaded.setAdapter(videoListViewAdapter);
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
        videoListViewAdapter.notifyDataSetChanged();
        lv_downloaded.invalidate();
    }

    @Override
    public void onPause() {
        DownloadController.detach(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        DownloadController.attach(this);
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}