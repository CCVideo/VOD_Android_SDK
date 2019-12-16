package com.bokecc.vod;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bokecc.sdk.mobile.util.HttpUtil;
import com.bokecc.vod.adapter.PlayVideoAdapter;
import com.bokecc.vod.data.DataSet;
import com.bokecc.vod.data.DataUtil;
import com.bokecc.vod.data.HuodeVideoInfo;
import com.bokecc.vod.download.DownloadController;
import com.bokecc.vod.download.DownloadListActivity;
import com.bokecc.vod.download.DownloadService;
import com.bokecc.vod.inter.SelectPlayer;
import com.bokecc.vod.play.MediaPlayActivity;
import com.bokecc.vod.play.SpeedPlayActivity;
import com.bokecc.vod.play.VrPlayActivity;
import com.bokecc.vod.upload.UploadController;
import com.bokecc.vod.upload.UploadManageActivity;
import com.bokecc.vod.upload.UploadService;
import com.bokecc.vod.utils.MultiUtils;
import com.bokecc.vod.view.HeadGridView;
import com.bokecc.vod.view.SelectPlayerDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private HeadGridView gv_video_list;
    private PlayVideoAdapter playVideoAdapter;
    private ImageView iv_account_info, iv_upload, iv_download, iv_main_img;
    private View headView;
    private LinearLayout ll_retry;
    private String verificationCode;
    private Button btn_retry;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private ArrayList<HuodeVideoInfo> videoDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MultiUtils.setStatusBarColor(this, R.color.transparent, true);
        initView();
        //初始化数据库和下载数据 没有开通授权播放和下载功能的账号 verificationCode可为空值
        verificationCode = MultiUtils.getVerificationCode();
        DownloadController.init(verificationCode);
        //初始化上传数据库
        UploadController.init();
        //申请存储权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 1);
        }
        //启动下载service
        Intent intent = new Intent(this, DownloadService.class);
        startService(intent);

        //启动上传service
        Intent uploadIntent = new Intent(this, UploadService.class);
        startService(uploadIntent);

        //请求数据
      initData();

    }

    private void initView() {
        gv_video_list = findViewById(R.id.gv_video_list);
        iv_account_info = findViewById(R.id.iv_account_info);
        iv_upload = findViewById(R.id.iv_upload);
        iv_download = findViewById(R.id.iv_download);
        ll_retry = findViewById(R.id.ll_retry);
        btn_retry = findViewById(R.id.btn_retry);
        headView = LayoutInflater.from(MainActivity.this).inflate(R.layout.headview_main, null);
        iv_main_img = headView.findViewById(R.id.iv_main_img);
        gv_video_list.addHeaderView(headView);

        videoDatas = new ArrayList<>();
        //配置自己的视频时取消这行注释
//        videoDatas = DataUtil.getVideoList();

        playVideoAdapter = new PlayVideoAdapter(MainActivity.this, videoDatas);
        gv_video_list.setAdapter(playVideoAdapter);
        iv_main_img.setImageResource(R.mipmap.iv_default_img);

        gv_video_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final HuodeVideoInfo item = (HuodeVideoInfo) playVideoAdapter.getItem(position);
                selectPlayer(item);
            }
        });

        iv_main_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoDatas != null && videoDatas.size() > 0) {
                    HuodeVideoInfo huodeVideoInfo = videoDatas.get(0);
                    selectPlayer(huodeVideoInfo);
                }
            }
        });

        iv_account_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AccountInfoActivity.class));
            }
        });

        iv_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DownloadListActivity.class));
            }
        });

        iv_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UploadManageActivity.class));
            }
        });

        btn_retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_retry.setVisibility(View.GONE);
                initData();
            }
        });

    }


    private void selectPlayer(final HuodeVideoInfo item) {
        String videoId = item.getVideoId();
        if (TextUtils.isEmpty(videoId)) {
            return;
        }
        SelectPlayerDialog selectPlayerDialog = new SelectPlayerDialog(MainActivity.this, new SelectPlayer() {
            @Override
            public void selectDWIjkMediaPlayer() {
                Intent playIntent = new Intent(MainActivity.this, SpeedPlayActivity.class);
                playIntent.putExtra("videoId", item.getVideoId());
                playIntent.putExtra("videoTitle", item.getVideoTitle());
                playIntent.putExtra("videoCover", item.getVideoCover());
                playIntent.putParcelableArrayListExtra("videoDatas", videoDatas);
                startActivity(playIntent);
            }

            @Override
            public void selectDWMediaPlayer() {
                Intent playIntent = new Intent(MainActivity.this, MediaPlayActivity.class);
                playIntent.putExtra("videoId", item.getVideoId());
                playIntent.putExtra("videoTitle", item.getVideoTitle());
                playIntent.putExtra("videoCover", item.getVideoCover());
                playIntent.putParcelableArrayListExtra("videoDatas", videoDatas);
                startActivity(playIntent);
            }

            @Override
            public void selectVrPlay() {
                Intent playIntent = new Intent(MainActivity.this, VrPlayActivity.class);
                playIntent.putExtra("videoId", item.getVideoId());
                playIntent.putExtra("videoTitle", item.getVideoTitle());
                playIntent.putExtra("videoCover", item.getVideoCover());
                playIntent.putParcelableArrayListExtra("videoDatas", videoDatas);
                startActivity(playIntent);
            }
        });
        selectPlayerDialog.show();
    }


    private void initData() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                request();
            }
        }).start();
    }

    private void request() {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(ConfigUtil.DATA_URL);
            connection = (HttpURLConnection) url.openConnection();
            //设置请求方法
            connection.setRequestMethod("GET");
            //设置连接超时时间（毫秒）
            connection.setConnectTimeout(5000);
            //设置读取超时时间（毫秒）
            connection.setReadTimeout(5000);
            //返回输入流
            InputStream in = connection.getInputStream();
            //读取输入流
            reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String result = sb.toString();
            if (!TextUtils.isEmpty(result)) {
                if (videoDatas != null && videoDatas.size() > 0) {
                    videoDatas.removeAll(videoDatas);
                }
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                    String videoTitle = jsonObject.getString("videoTitle");
                    String videoId = jsonObject.getString("videoId");
                    String videoTime = jsonObject.getString("videoTime");
                    String videoCover = jsonObject.getString("videoCover");
                    HuodeVideoInfo huodeVideoInfo = new HuodeVideoInfo(videoTitle, videoId, videoTime, videoCover);
                    videoDatas.add(huodeVideoInfo);
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    playVideoAdapter.notifyDataSetChanged();
                    HuodeVideoInfo huodeVideoInfo = videoDatas.get(0);
                    if (huodeVideoInfo != null) {
                        MultiUtils.showCornerVideoCover(iv_main_img, huodeVideoInfo.getVideoCover());
                    }
                }
            });
        } catch (Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ll_retry.setVisibility(View.VISIBLE);
                }
            });
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {//关闭连接
                connection.disconnect();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataSet.saveDownloadData();
        DataSet.saveUploadData();
    }
}
