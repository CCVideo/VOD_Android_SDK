package com.bokecc.sdk.mobile.demo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.bokecc.sdk.mobile.demo.download.DownloadFragment;
import com.bokecc.sdk.mobile.demo.download.DownloadListActivity;
import com.bokecc.sdk.mobile.demo.download.DownloadService;
import com.bokecc.sdk.mobile.demo.downloadutil.DownloadController;
import com.bokecc.sdk.mobile.demo.downloadutil.DownloaderWrapper;
import com.bokecc.sdk.mobile.demo.play.PlayFragment;
import com.bokecc.sdk.mobile.demo.upload.UploadFragment;
import com.bokecc.sdk.mobile.demo.util.ConfigUtil;
import com.bokecc.sdk.mobile.demo.util.DataSet;
import com.bokecc.sdk.mobile.demo.util.LogcatHelper;
import com.bokecc.sdk.mobile.download.Downloader;
import com.bokecc.sdk.mobile.util.HttpUtil;
import com.bokecc.sdk.mobile.util.HttpUtil.HttpLogLevel;

/**
 * Demo主界面，包括播放、上传、下载三个标签页
 *
 * @author CC视频
 */
@SuppressLint("NewApi")
public class MainActivity extends FragmentActivity implements TabListener {

    private ViewPager viewPager;

    private static String[] TAB_TITLE = {"播放", "上传", "下载"};

    private TabFragmentPagerAdapter adapter;

    private  String verificationCode;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //进入到这里代表没有权限.
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 1);
        }

        LogcatHelper.getInstance(this).start();
        HttpUtil.LOG_LEVEL = HttpLogLevel.DETAIL;
        viewPager = (ViewPager) this.findViewById(R.id.pager);
        initView();


        getExternalFilesDir(null);

        //初始化数据库和下载数据
        DataSet.init(getApplicationContext());
        DownloadController.init(verificationCode);

        //启动后台下载service
        Intent intent = new Intent(this, DownloadService.class);
        startService(intent);
    }

    @SuppressWarnings("deprecation")
    private void initView() {

        final ActionBar actionBar = getActionBar();

        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        adapter = new TabFragmentPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        for (int i = 0; i < ConfigUtil.MAIN_FRAGMENT_MAX_TAB_SIZE; i++) {
            Tab tab = actionBar.newTab();
            tab.setText(TAB_TITLE[i]).setTabListener(this);
            actionBar.addTab(tab);
        }

        viewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                actionBar.setSelectedNavigationItem(arg0);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    public static class TabFragmentPagerAdapter extends FragmentPagerAdapter {

        private Fragment[] fragments;

        public TabFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
            fragments = new Fragment[]{new PlayFragment(), new UploadFragment(), new DownloadFragment()};
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];

        }

        @Override
        public int getCount() {
            return ConfigUtil.MAIN_FRAGMENT_MAX_TAB_SIZE;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.inputVerCode:
                int currentItem = viewPager.getCurrentItem();
                if (currentItem==0){
                    PlayFragment fragment = (PlayFragment) adapter.fragments[0];
                    fragment.showInputCodeDialog();
                }else if (currentItem==2){
                    DownloadFragment downloadFragment = (DownloadFragment) adapter.fragments[2];
                    downloadFragment.showInputCodeDialog();
                }
                break;

            case R.id.inputVideoId:
                int currentItemPosition = viewPager.getCurrentItem();
                if (currentItemPosition==0){
                    PlayFragment fragment = (PlayFragment) adapter.fragments[0];
                    fragment.showInputVideoIdDialog();
                }
                break;
            case R.id.downloadItem:
                startActivity(new Intent(getApplicationContext(), DownloadListActivity.class));
                break;
            case R.id.accountInfo:
                startActivity(new Intent(getApplicationContext(), AccountInfoActivity.class));
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onBackPressed() {
        Log.i("data", "save data... ...");

        DataSet.saveUploadData();
        DataSet.saveDownloadData();

        LogcatHelper.getInstance(this).stop();

        if (hasDownloadingTask()) {
            showDialog();
        } else {
            super.onBackPressed();
        }
    }

    private boolean hasDownloadingTask() {
        for (DownloaderWrapper wrapper : DownloadController.downloadingList) {
            if (wrapper.getStatus() == Downloader.DOWNLOAD || wrapper.getStatus() == Downloader.WAIT) {
                return true;
            }
        }

        return false;
    }

    private void showDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DownloadController.setBackDownload(true);
                        finish();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        stopDownloadService();
                        DownloadController.setBackDownload(false);
                        finish();
                    }
                }).setTitle("有正在下载的任务，是否需要后台下载？")
                .create();

        dialog.show();
    }

    private void stopDownloadService() {
        Intent intent = new Intent(this, DownloadService.class);
        stopService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
