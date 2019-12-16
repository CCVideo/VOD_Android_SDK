package com.bokecc.vod;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.bokecc.sdk.mobile.drm.DRMServer;
import com.bokecc.sdk.mobile.util.DWSdkStorage;
import com.bokecc.sdk.mobile.util.DWStorageUtil;
import com.bokecc.vod.data.ObjectBox;

public class HuodeApplication extends Application {

    private static DRMServer drmServer;
    public static Context context;
    private static int drmServerPort;
    public static SharedPreferences sp;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        sp = getSharedPreferences("AccountSettings", Context.MODE_PRIVATE);
        //初始化本地数据库
        ObjectBox.init(this);
        initDWStorage();
        startDRMServer();
    }

    public static Context getContext() {
        return context;
    }

    public static SharedPreferences getSp(){
        return sp;
    }
    // 启动DRMServer
    public void startDRMServer() {
        if (drmServer == null) {
            drmServer = new DRMServer();
            drmServer.setRequestRetryCount(20);
        }

        try {
            drmServer.start();
            setDrmServerPort(drmServer.getPort());
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "启动解密服务失败，请检查网络限制情况:"+e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initDWStorage() {
        DWSdkStorage myDWSdkStorage = new DWSdkStorage() {
            private SharedPreferences sp = getApplicationContext().getSharedPreferences("mystorage", MODE_PRIVATE);

            @Override
            public void put(String key, String value) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(key, value);
                editor.commit();
            }

            @Override
            public String get(String key) {
                return sp.getString(key, "");
            }
        };

        DWStorageUtil.setDWSdkStorage(myDWSdkStorage);
    }

    @Override
    public void onTerminate() {
        if (drmServer != null) {
            drmServer.stop();
        }
        super.onTerminate();
    }

    public static int getDrmServerPort() {
        return drmServerPort;
    }

    public void setDrmServerPort(int drmServerPort) {
        this.drmServerPort = drmServerPort;
    }

    public static DRMServer getDRMServer() {
        return drmServer;
    }
}
