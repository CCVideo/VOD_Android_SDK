package com.bokecc.vod.upload;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bokecc.sdk.mobile.upload.VideoInfo;
import com.bokecc.vod.ConfigUtil;
import com.bokecc.vod.R;
import com.bokecc.vod.data.UploadInfo;
import com.bokecc.vod.inter.SelectCompressLevel;
import com.bokecc.vod.upload.compress.VideoCompress;
import com.bokecc.vod.utils.MultiUtils;
import com.bokecc.vod.view.CompressProgressDialog;
import com.bokecc.vod.view.SelectCompressLevelDialog;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class EditVideoInfoActivity extends Activity implements View.OnClickListener {
    private ImageView iv_back;
    private String videoPath, compressOutputPath,videoTitle,uploadId,videoAbstract,videoCoverPath;
    private Activity activity;
    private TextView tv_select_compress_level,tv_confirm_upload,tv_userid,tv_key;
    private EditText et_video_title,et_video_abstract;
    //0：不压缩 1：高质量压缩 2：中质量压缩 3：低质量压缩
    private int compressLecel = 0;
    private CompressProgressDialog compressProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_video_info);
        activity = this;
        MultiUtils.setStatusBarColor(this, R.color.transparent, true);
        initView();
    }

    private void initView() {
        videoPath = getIntent().getStringExtra("videoPath");
        iv_back = findViewById(R.id.iv_back);
        tv_select_compress_level = findViewById(R.id.tv_select_compress_level);
        tv_confirm_upload = findViewById(R.id.tv_confirm_upload);
        tv_userid = findViewById(R.id.tv_userid);
        tv_key = findViewById(R.id.tv_key);
        et_video_title = findViewById(R.id.et_video_title);
        et_video_abstract = findViewById(R.id.et_video_abstract);

        tv_userid.setText(ConfigUtil.USERID);
        tv_key.setText(ConfigUtil.API_KEY);
        iv_back.setOnClickListener(this);
        tv_select_compress_level.setOnClickListener(this);
        tv_confirm_upload.setOnClickListener(this);

        Bitmap videoThumbnail = MultiUtils.getVideoThumbnail(videoPath, MultiUtils.dipToPx(activity, 120), MultiUtils.dipToPx(activity, 67));
        videoCoverPath = MultiUtils.saveBitmapToLocal(videoThumbnail);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_confirm_upload:
                uploadVideo();
                break;
            case R.id.tv_select_compress_level:
                SelectCompressLevelDialog selectCompressLevelDialog = new SelectCompressLevelDialog(activity, new SelectCompressLevel() {
                    @Override
                    public void selectedCompressLevel(int level) {
                        if (level == 0) {
                            compressLecel = 0;
                            tv_select_compress_level.setText("不压缩");
                        } else if (level == 1) {
                            compressLecel = 1;
                            tv_select_compress_level.setText("高质量压缩");
                        } else if (level == 2) {
                            compressLecel = 2;
                            tv_select_compress_level.setText("中质量压缩");
                        } else if (level == 3) {
                            compressLecel = 3;
                            tv_select_compress_level.setText("低质量压缩");
                        }
                    }
                });
                selectCompressLevelDialog.show();
                break;
        }
    }

    private void uploadVideo() {
        if (TextUtils.isEmpty(videoPath)) {
            MultiUtils.showToast(activity, "请选择视频");
            return;
        }

        videoTitle = MultiUtils.getEditTextContent(et_video_title);
        if (TextUtils.isEmpty(videoTitle)) {
            MultiUtils.showToast(activity, "请输入视频标题");
            return;
        }
        videoAbstract = MultiUtils.getEditTextContent(et_video_abstract);
        if (compressLecel == 0) {
            uploadId = UploadInfo.UPLOAD_PRE.concat(System.currentTimeMillis() + "");
            startUpload(uploadId,videoPath);
        } else if (compressLecel == 1) {
            createOutputPath();
            VideoCompress.compressVideoHigh(videoPath, compressOutputPath, new VideoCompress.CompressListener() {
                @Override
                public void onStart() {
                    compressProgressDialog.show();
                }

                @Override
                public void onSuccess() {
                    compressProgressDialog.dismiss();
                    MultiUtils.showToast(activity,"压缩成功");
                    startUpload(uploadId,compressOutputPath);
                }

                @Override
                public void onFail() {
                    compressProgressDialog.dismiss();
                    MultiUtils.showToast(activity,"压缩失败，请重试");
                }

                @Override
                public void onProgress(float percent) {
                    compressProgressDialog.updateProgress(percent);
                }
            });
        } else if (compressLecel == 2) {
            createOutputPath();
            VideoCompress.compressVideoMedium(videoPath, compressOutputPath, new VideoCompress.CompressListener() {
                @Override
                public void onStart() {
                    compressProgressDialog.show();
                }

                @Override
                public void onSuccess() {
                    compressProgressDialog.dismiss();
                    MultiUtils.showToast(activity,"压缩成功");
                    startUpload(uploadId,compressOutputPath);
                }

                @Override
                public void onFail() {
                    compressProgressDialog.dismiss();
                    MultiUtils.showToast(activity,"压缩失败，请重试");
                }

                @Override
                public void onProgress(float percent) {
                    compressProgressDialog.updateProgress(percent);
                }
            });
        } else if (compressLecel == 3) {
            createOutputPath();
            VideoCompress.compressVideoLow(videoPath, compressOutputPath, new VideoCompress.CompressListener() {
                @Override
                public void onStart() {
                    compressProgressDialog.show();
                }

                @Override
                public void onSuccess() {
                    compressProgressDialog.dismiss();
                    MultiUtils.showToast(activity,"压缩成功");
                    startUpload(uploadId,compressOutputPath);
                }

                @Override
                public void onFail() {
                    compressProgressDialog.dismiss();
                    MultiUtils.showToast(activity,"压缩失败，请重试");
                }

                @Override
                public void onProgress(float percent) {
                    compressProgressDialog.updateProgress(percent);
                }
            });
        }
    }

    //创建压缩输出的路径
    private void createOutputPath() {
        String savePath = Environment.getExternalStorageDirectory().getPath() + "/CompressVideo/";
        File file = new File(savePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        File cropVideo = new File(savePath, "compressout.mp4");
        if (cropVideo.exists()) {
            cropVideo.delete();
        }
        compressOutputPath = cropVideo.getAbsolutePath();
        compressProgressDialog = new CompressProgressDialog(activity);
        compressProgressDialog.setCancelable(false);
        uploadId = UploadInfo.UPLOAD_PRE.concat(System.currentTimeMillis() + "");
    }



    private void startUpload(String uploadId, String videoPath) {
        UploadInfo newUploadInfo = new UploadInfo();
        newUploadInfo.setUploadId(uploadId);
        newUploadInfo.setTitle(videoTitle);
        newUploadInfo.setDesc(videoAbstract);
        newUploadInfo.setFilePath(videoPath);
        newUploadInfo.setVideoCoverPath(videoCoverPath);
        UploadController.insertUploadInfo(newUploadInfo);
        finish();
    }

}
