package com.bokecc.vod.upload;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bokecc.vod.R;
import com.bokecc.vod.utils.MultiUtils;

public class UploadManageActivity extends FragmentActivity implements View.OnClickListener {
    private ViewPager vp_upload;
    private ImageView iv_back,iv_upload;
    public static String[] TAB_TITLE = {"上传中","已完成"};
    private UploadFragmentPagerAdapter adapter;
    private TextView tv_uploading,tv_upload_done;
    private View line_uploading,line_upload_done;
    private String videoPath;
    private int SELECTVIDEOCODE = 1;
    private Activity activity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_manage);
        MultiUtils.setStatusBarColor(this,R.color.transparent,true);
        activity = this;
        initView();
    }

    private void initView() {
        iv_back = findViewById(R.id.iv_back);
        iv_upload = findViewById(R.id.iv_upload);
        vp_upload = findViewById(R.id.vp_upload);
        tv_uploading = findViewById(R.id.tv_uploading);
        tv_upload_done = findViewById(R.id.tv_upload_done);
        line_uploading = findViewById(R.id.line_uploading);
        line_upload_done = findViewById(R.id.line_upload_done);

        iv_back.setOnClickListener(this);
        iv_upload.setOnClickListener(this);
        tv_uploading.setOnClickListener(this);
        tv_upload_done.setOnClickListener(this);

        adapter = new UploadFragmentPagerAdapter(getSupportFragmentManager());
        vp_upload.setAdapter(adapter);

        vp_upload.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                if (i==0){
                    selectUploading();
                }else if (i==1){
                    selectUploadDone();
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    public static class UploadFragmentPagerAdapter extends FragmentPagerAdapter {

        private Fragment[] fragments;

        public UploadFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
            fragments = new Fragment[]{new UploadingFragment(),new UploadDoneFragment()};
        }

        @Override
        public Fragment getItem(int arg0) {
            return fragments[arg0];
        }

        @Override
        public int getCount() {

            return 2;
        }

        @Override
        public CharSequence getPageTitle(int index) {
            return TAB_TITLE[index];
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_upload:
                selectVideo();
                break;
            case R.id.tv_uploading:
                selectUploading();
                vp_upload.setCurrentItem(0);
                break;
            case R.id.tv_upload_done:
                selectUploadDone();
                vp_upload.setCurrentItem(1);
                break;
        }
    }

    //选择视频
    private void selectVideo() {
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        }
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "请选择一个视频文件"), SELECTVIDEOCODE);
        } catch (android.content.ActivityNotFoundException ex) {
            MultiUtils.showToast(UploadManageActivity.this, "请安装文件管理器");
        }
    }

    private void selectUploading() {
        tv_uploading.setTextColor(getResources().getColor(R.color.orange));
        tv_upload_done.setTextColor(getResources().getColor(R.color.gray));
        line_uploading.setVisibility(View.VISIBLE);
        line_upload_done.setVisibility(View.INVISIBLE);
    }

    private void selectUploadDone() {
        tv_uploading.setTextColor(getResources().getColor(R.color.gray));
        tv_upload_done.setTextColor(getResources().getColor(R.color.orange));
        line_uploading.setVisibility(View.INVISIBLE);
        line_upload_done.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == SELECTVIDEOCODE) {
            Uri uri = data.getData();
            if (Build.VERSION.SDK_INT >= 19) {
                videoPath = MultiUtils.getPath_above19(activity, uri);
            } else {
                videoPath = MultiUtils.getFilePath_below19(activity, uri);
            }

            if (videoPath == null) {
                MultiUtils.showToast(activity, "文件有误，请重新选择");
                return;
            }
            startActivity(new Intent(activity,EditVideoInfoActivity.class).putExtra("videoPath",videoPath));
        }

    }

    protected void onResume() {
        super.onResume();
        //启动上传service
        Intent intent = new Intent(this,UploadService.class);
        startService(intent);
    }
}
