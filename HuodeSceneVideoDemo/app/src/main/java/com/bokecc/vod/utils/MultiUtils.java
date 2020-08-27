package com.bokecc.vod.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bokecc.sdk.mobile.play.DanmuInfo;
import com.bokecc.vod.ConfigUtil;
import com.bokecc.vod.HuodeApplication;
import com.bokecc.vod.R;
import com.bokecc.vod.data.DanmuColorInfo;
import com.bokecc.vod.view.RoundCornerTransform;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.WIFI_SERVICE;

/**
 *
 */
public class MultiUtils {


    private static String DOWNLOAD_CONTENT = "content://downloads/public_downloads";

    public static void showToast(Activity activity, final String content) {
        if (activity != null && !activity.isFinishing()) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(HuodeApplication.getContext(), content, Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    public static void showTopToast(Activity activity, final String content) {
        if (activity != null && !activity.isFinishing()) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(HuodeApplication.getContext(), content, Toast.LENGTH_SHORT);
                    int yOffset = dipToPx(HuodeApplication.getContext(), 100);
                    toast.setGravity(Gravity.TOP, 0, yOffset);
                    toast.show();
                }
            });
        }
    }

    public static void setStatusBarColor(Activity activity, int color, boolean isDarkStatusBar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            int option;
            if (isDarkStatusBar) {
                option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            }
            window.getDecorView().setSystemUiVisibility(option);

            window.getDecorView().setSystemUiVisibility(option);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(activity.getResources().getColor(color));
        }
    }

    //将dp转化为sp
    public static int dipToPx(Context context, float dip) {
        return (int) (dip * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    // 将毫秒转为分钟：秒
    public static String millsecondsToMinuteSecondStr(long ms) {
        int seconds = (int) (ms / 1000);
        String result = "";
        int min = 0, second = 0;
        min = seconds / 60;
        second = seconds - min * 60;

        if (min < 10) {
            result += "0" + min + ":";
        } else {
            result += min + ":";
        }
        if (second < 10) {
            result += "0" + second;
        } else {
            result += second;
        }
        return result;
    }

    public static boolean isActivityAlive(Activity activity) {
        if (activity != null && !activity.isFinishing()) {
            return true;
        }
        return false;
    }

    //获得屏幕可用的宽度
    public static int getScreenWidth(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        int height = dm.widthPixels;
        return height;
    }

    //获得屏幕可用的高度
    public static int getScreenHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        int height = dm.heightPixels;
        return height;
    }

    //展示视频封面图片
    public static void showVideoCover(ImageView imageView, String imgUrl) {
        if (TextUtils.isEmpty(imgUrl)) {
            imageView.setImageResource(R.mipmap.iv_default_img);
        } else {
            Glide.with(HuodeApplication.getContext()).load(imgUrl).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.color.dialogBac).into(imageView);
        }
    }

    //展示圆角图片
    public static void showCornerVideoCover(ImageView imageView, String imgUrl) {
        if (TextUtils.isEmpty(imgUrl)) {
            imageView.setImageResource(R.mipmap.iv_default_img);
        } else {
            Glide.with(HuodeApplication.getContext()).load(imgUrl).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.color.dialogBac).transform(new CenterCrop(HuodeApplication.getContext()), new RoundCornerTransform(HuodeApplication.getContext(), 10)).into(imageView);
        }
    }

    /**
     * 设置应用界面亮度
     *
     * @param brightnessValue 取值范围0-255
     */
    public static void setBrightness(Activity context, int brightnessValue) {
        Window localWindow = context.getWindow();
        WindowManager.LayoutParams localLayoutParams = localWindow.getAttributes();
        localLayoutParams.screenBrightness = brightnessValue / 255.0F;
        localWindow.setAttributes(localLayoutParams);
    }

    /**
     * 获得系统亮度
     *
     * @param context
     * @return
     */
    public static int getSystemBrightness(Activity context) {
        ContentResolver contentResolver = context.getContentResolver();
        int defVal = 125;
        return Settings.System.getInt(contentResolver,
                Settings.System.SCREEN_BRIGHTNESS, defVal);
    }

    public static void setVerificationCode(String verificationCode) {
        HuodeApplication.getSp().edit().putString("verificationCode", verificationCode).commit();
    }

    public static String getVerificationCode() {
        String verificationCode = HuodeApplication.getSp().getString("verificationCode", "");
        return verificationCode;
    }

    public static void setIsReadExerciseGuide(boolean isRead) {
        HuodeApplication.getSp().edit().putBoolean("isReadExerciseGuide", isRead).commit();
    }

    public static boolean getIsReadExerciseGuide() {
        boolean isReadExerciseGuide = HuodeApplication.getSp().getBoolean("isReadExerciseGuide", false);
        return isReadExerciseGuide;
    }

    public static void setIsDynamicVideo(boolean isDynamicVideo) {
        HuodeApplication.getSp().edit().putBoolean("isDynamicVideo", isDynamicVideo).commit();
    }

    public static boolean getIsDynamicVideo() {
        boolean isDynamicVideo = HuodeApplication.getSp().getBoolean("isDynamicVideo", true);
        return isDynamicVideo;
    }

    //    获得输入框内容
    public static String getEditTextContent(EditText editText) {
        return editText.getText().toString().trim().replace(" ", "");
    }

    public static float calFloat(int scale, int num1, int num2) {
        BigDecimal bigDecimal1 = new BigDecimal(String.valueOf(num1));
        BigDecimal bigDecimal2 = new BigDecimal(String.valueOf(num2));
        return bigDecimal1.divide(bigDecimal2, scale, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    //获取视频图片
    public static Bitmap getVideoThumbnail(String url, int width, int height) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        int kind = MediaStore.Video.Thumbnails.MINI_KIND;
        try {
            if (Build.VERSION.SDK_INT >= 14) {
                if ((url.startsWith("http://")
                        || url.startsWith("https://")
                        || url.startsWith("widevine://"))) {
                    retriever.setDataSource(url, new HashMap<String, String>());
                } else {
                    retriever.setDataSource(url);
                }

            } else {
                retriever.setDataSource(url);
            }
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }
        if (kind == MediaStore.Images.Thumbnails.MICRO_KIND && bitmap != null) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        return bitmap;
    }

    public static String saveBitmapToLocal(Bitmap bitmap) {
        String imgPath = createDownloadPath();
        String imgName = System.currentTimeMillis() + "uploadCover.jpg";
        File imgFile = new File(imgPath, imgName);
        if (imgFile.exists()) {
            imgFile.delete();
        }

        try {
            FileOutputStream out = new FileOutputStream(imgFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            return imgFile.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String byteToM(long num) {
        double m = (double) num / 1024 / 1024;
        return String.format("%.2f", m);
    }

    //创建下载路径
    public static String createDownloadPath() {
        // 判断sd卡是否存在
        String path = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            File sdDir = Environment.getExternalStorageDirectory();// 获取根目录
            File dir = new File(sdDir + "/" + ConfigUtil.DOWNLOAD_PATH);
            if (!dir.exists()) {
                dir.mkdir();
            }
            path = dir + "/";
        }

        return path;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath_above19(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");

                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                } else {
                    return getRealStoragePath(context, "/" + split[1]);
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse(DOWNLOAD_CONTENT), Long.parseLong(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    //反射循环判断文件的真实路径
    public static String getRealStoragePath(Context context, String pathTail) {
        try {
            StorageManager sm = (StorageManager) context.getSystemService(context.STORAGE_SERVICE);
            Method getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths");
            getVolumePathsMethod.setAccessible(true);
            Object result = getVolumePathsMethod.invoke(sm);

            if (result != null && result instanceof String[]) {
                String[] paths = (String[]) result;
                for (String path : paths) {
                    if (new File(path + pathTail).exists()) {
                        return path + pathTail;
                    }
                }
            }

        } catch (Exception e) {
            Log.e("demo", "getSecondaryStoragePath() failed", e);
        }

        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * API19以下获取图片路径的方法
     *
     * @param uri
     */
    public static String getFilePath_below19(Context context, Uri uri) {
        //这里开始的第二部分，获取图片的路径：低版本的是没问题的，但是sdk>19会获取不到
        String[] proj = {MediaStore.Images.Media.DATA};
        //好像是android多媒体数据库的封装接口，具体的看Android文档
        Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
        //获得用户选择的图片的索引值
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        //将光标移至开头 ，这个很重要，不小心很容易引起越界
        cursor.moveToFirst();
        //最后根据索引值获取图片路径   结果类似：/mnt/sdcard/DCIM/Camera/IMG_20151124_013332.jpg
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }

    public static int getNetWorkStatus(Context context) {
        int netWorkStatus = 1;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo dataInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (wifiInfo.isConnected() && dataInfo.isConnected()) {
                //wifi和移动数据同时连接
                netWorkStatus = 3;
            } else if (wifiInfo.isConnected() && !dataInfo.isConnected()) {
                //wifi已连接，移动数据断开
                netWorkStatus = 1;
            } else if (!wifiInfo.isConnected() && dataInfo.isConnected()) {
                //wifi断开 移动数据连接
                netWorkStatus = 2;
            } else {
                //wifi断开 移动数据断开
                netWorkStatus = 0;
            }
        } else {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Network[] networks = connectivityManager.getAllNetworks();
            int nets = 0;
            for (int i = 0; i < networks.length; i++) {
                NetworkInfo netInfo = connectivityManager.getNetworkInfo(networks[i]);
                if (netInfo.getType() == ConnectivityManager.TYPE_MOBILE && !netInfo.isConnected()) {
                    nets += 1;
                }

                if (netInfo.getType() == ConnectivityManager.TYPE_MOBILE && netInfo.isConnected()) {
                    nets += 2;
                }

                if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    nets += 4;
                }
            }

            switch (nets) {
                case 0:
                    //wifi断开 移动数据断开
                    netWorkStatus = 0;
                    Log.i("dwdemo", "wifi断开 移动数据断开");
                    break;
                case 2:
                    //wifi断开 移动数据连接
                    netWorkStatus = 2;
                    break;
                case 4:
                    //wifi已连接，移动数据断开
                    netWorkStatus = 1;
                    break;
                case 5:
                    //wifi和移动数据同时连接
                    netWorkStatus = 3;
                    break;
            }
        }

        return netWorkStatus;
    }

    public static String millsecondsToStr(int seconds) {
        seconds = seconds / 1000;
        String result = "";
        int hour = 0, min = 0, second = 0;
        hour = seconds / 3600;
        min = (seconds - hour * 3600) / 60;
        second = seconds - hour * 3600 - min * 60;
        if (hour < 10) {
            result += "0" + hour + ":";
        } else {
            result += hour + ":";
        }
        if (min < 10) {
            result += "0" + min + ":";
        } else {
            result += min + ":";
        }
        if (second < 10) {
            result += "0" + second;
        } else {
            result += second;
        }
        return result;
    }


    public static String getConnectWifiName(Context context) {
        String ssid = null;
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = null;
        if (wifiManager != null) {
            wifiInfo = wifiManager.getConnectionInfo();
        }
        if (wifiInfo != null) {
            ssid = wifiInfo.getSSID();
            int networkId = wifiInfo.getNetworkId();
            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration wifiConfiguration : configuredNetworks) {
                if (wifiConfiguration.networkId == networkId) {
                    ssid = wifiConfiguration.SSID;
                    break;
                }
            }
            return ssid;
        } else {
            return null;
        }
    }

    public static int getMaxValue(int x, int y, int z) {
        int max = 0;
        if (x > y && x > z) {
            max = x;
        } else if (y > x && y > z) {
            max = y;
        } else if (z > x && z > y) {
            max = z;
        }
        return max;
    }

    public static String getVideoScreenShotOutPath() {
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            File dirPath = new File(absolutePath, ConfigUtil.DOWNLOAD_PATH);
            if (!dirPath.exists()) {
                dirPath.mkdirs();
            }
            String outPath = dirPath.getAbsolutePath() + File.separator + System.currentTimeMillis() + ".jpg";
            return outPath;
        } else {
            return null;
        }

    }

    private static long lastTotalRxBytes = 0;
    private static long lastTime = 0;

    public static String getNetSpeed(int uid) {
        long currentTotalRxBytes = getTotalRxBytes(uid);
        long currentTime = System.currentTimeMillis();
        long kbSpeed = ((currentTotalRxBytes - lastTotalRxBytes) * 1000 / (currentTime - lastTime));
        lastTime = currentTime;
        lastTotalRxBytes = currentTotalRxBytes;
        if (kbSpeed > 1024) {
            float mbSpeed = calMbSpeed(2, kbSpeed, 1024);
            return mbSpeed + "MB/s";
        } else {
            return kbSpeed + "KB/s";
        }
    }


    public static long getTotalRxBytes(int uid) {
        return TrafficStats.getUidRxBytes(uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);
    }

    public static float calMbSpeed(int scale, long num1, long num2) {
        BigDecimal bigDecimal1 = new BigDecimal(String.valueOf(num1));
        BigDecimal bigDecimal2 = new BigDecimal(String.valueOf(num2));
        return bigDecimal1.divide(bigDecimal2, scale, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    //隐藏软键盘
    public static void hideSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    //显示软键盘
    public static void showSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static List<DanmuColorInfo> getDanmuColorInfoDatas() {
        List<DanmuColorInfo> datas = new ArrayList<>();

        DanmuColorInfo danmuColorInfo1 = new DanmuColorInfo();
        danmuColorInfo1.setNormalImgRes(R.mipmap.danmu_color_one_normal);
        danmuColorInfo1.setSelectdImgRes(R.mipmap.danmu_color_one_selected);
        danmuColorInfo1.setSelected(false);
        //颜色值为0x开头的小写字母组成 如0xffffff
        danmuColorInfo1.setColor("0xffffff");
        datas.add(danmuColorInfo1);

        DanmuColorInfo danmuColorInfo2 = new DanmuColorInfo();
        danmuColorInfo2.setNormalImgRes(R.mipmap.danmu_color_two_normal);
        danmuColorInfo2.setSelectdImgRes(R.mipmap.danmu_color_two_selected);
        danmuColorInfo2.setSelected(false);
        danmuColorInfo2.setColor("0x999999");
        datas.add(danmuColorInfo2);

        DanmuColorInfo danmuColorInfo3 = new DanmuColorInfo();
        danmuColorInfo3.setNormalImgRes(R.mipmap.danmu_color_three_normal);
        danmuColorInfo3.setSelectdImgRes(R.mipmap.danmu_color_three_selected);
        danmuColorInfo3.setSelected(false);
        danmuColorInfo3.setColor("0xe6151e");
        datas.add(danmuColorInfo3);

        DanmuColorInfo danmuColorInfo4 = new DanmuColorInfo();
        danmuColorInfo4.setNormalImgRes(R.mipmap.danmu_color_four_normal);
        danmuColorInfo4.setSelectdImgRes(R.mipmap.danmu_color_four_selected);
        danmuColorInfo4.setSelected(false);
        danmuColorInfo4.setColor("0x9d22b1");
        datas.add(danmuColorInfo4);

        DanmuColorInfo danmuColorInfo5 = new DanmuColorInfo();
        danmuColorInfo5.setNormalImgRes(R.mipmap.danmu_color_five_normal);
        danmuColorInfo5.setSelectdImgRes(R.mipmap.danmu_color_five_selected);
        danmuColorInfo5.setSelected(false);
        danmuColorInfo5.setColor("0x6738b8");
        datas.add(danmuColorInfo5);

        DanmuColorInfo danmuColorInfo6 = new DanmuColorInfo();
        danmuColorInfo6.setNormalImgRes(R.mipmap.danmu_color_six_normal);
        danmuColorInfo6.setSelectdImgRes(R.mipmap.danmu_color_six_selected);
        danmuColorInfo6.setSelected(false);
        danmuColorInfo6.setColor("0x3d50b6");
        datas.add(danmuColorInfo6);

        DanmuColorInfo danmuColorInfo7 = new DanmuColorInfo();
        danmuColorInfo7.setNormalImgRes(R.mipmap.danmu_color_seven_normal);
        danmuColorInfo7.setSelectdImgRes(R.mipmap.danmu_color_seven_selected);
        danmuColorInfo7.setSelected(false);
        danmuColorInfo7.setColor("0x03a9f4");
        datas.add(danmuColorInfo7);

        DanmuColorInfo danmuColorInfo8 = new DanmuColorInfo();
        danmuColorInfo8.setNormalImgRes(R.mipmap.danmu_color_eight_normal);
        danmuColorInfo8.setSelectdImgRes(R.mipmap.danmu_color_eight_selected);
        danmuColorInfo8.setSelected(false);
        danmuColorInfo8.setColor("0x009688");
        datas.add(danmuColorInfo8);

        DanmuColorInfo danmuColorInfo9 = new DanmuColorInfo();
        danmuColorInfo9.setNormalImgRes(R.mipmap.danmu_color_nine_normal);
        danmuColorInfo9.setSelectdImgRes(R.mipmap.danmu_color_nine_selected);
        danmuColorInfo9.setSelected(false);
        danmuColorInfo9.setColor("0x259b24");
        datas.add(danmuColorInfo9);

        DanmuColorInfo danmuColorInfo10 = new DanmuColorInfo();
        danmuColorInfo10.setNormalImgRes(R.mipmap.danmu_color_ten_normal);
        danmuColorInfo10.setSelectdImgRes(R.mipmap.danmu_color_ten_selected);
        danmuColorInfo10.setSelected(false);
        danmuColorInfo10.setColor("0x8bc34a");
        datas.add(danmuColorInfo10);

        return datas;
    }
}
