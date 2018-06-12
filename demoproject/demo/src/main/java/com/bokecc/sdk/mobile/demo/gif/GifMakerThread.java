package com.bokecc.sdk.mobile.demo.gif;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import com.bumptech.glide.gifencoder.AnimatedGifEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liufh on 2017/9/26.
 */

public class GifMakerThread extends Thread {

    public interface GifMakerListener {
        void onGifFinish();
        void onGifError(Exception e);
    }

    AnimatedGifEncoder localAnimatedGifEncoder = new AnimatedGifEncoder();
    GifMakerListener listener;
    String path;
    int delay, repeat;
    public GifMakerThread(GifMakerListener listener, String path, int delay, int repeat) {
        this.listener = listener;
        this.path = path;
        this.delay = delay;
        this.repeat = repeat;

        File pathFile = new File(path);

        if (pathFile.exists()) {
            pathFile.delete();
        }

        File parentFile = pathFile.getParentFile();
        parentFile.mkdirs();

    }

    List<Bitmap> list = new ArrayList<>();

    Object mSync = new Object();

    boolean isStart = false;
    public void startGif() {
        isStart = true;
        isCancel = false;
        list.clear();
        start();
    }

    public void stopGif() {
        isStart = false;

    }

    boolean isCancel = false;
    public void cancelGif() {
        synchronized (mSync) {
            list.clear();
            isStart = false;
            isCancel = true;
            listener = null;
        }
    }

    public void addBitmap(Bitmap bitmap) {
        synchronized (mSync) {
            list.add(bitmap);
        }

    }

    @Override
    public void run(){

        ByteArrayOutputStream baos = null;
        FileOutputStream fos = null;
        try {
            baos = new ByteArrayOutputStream();
            localAnimatedGifEncoder.start(baos);//start
            localAnimatedGifEncoder.setRepeat(repeat);//设置生成gif的开始播放时间。0为立即开始播放
            localAnimatedGifEncoder.setDelay(delay);


            while(true) {

                Bitmap bitmap = null;
                synchronized (mSync) {
                    if (list.size() > 0) {
                        bitmap = list.remove(0);
                    }
                }

                if (bitmap != null) {
                    localAnimatedGifEncoder.addFrame(bitmap);
                }

                if (list.size() == 0 && !isStart) {
                    break;
                }

                if (isCancel) {
                    return;
                }

            }

            localAnimatedGifEncoder.finish();//finish

            fos = new FileOutputStream(path);
            baos.writeTo(fos);
            baos.flush();
            fos.flush();

            synchronized (mSync) {
                if (listener != null) {
                    listener.onGifFinish();
                }
            }

        } catch (IOException e) {
            Log.e("GifMakerThread", e.getLocalizedMessage());

            if (listener != null) {
                listener.onGifError(e);
            }

        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    //使用Bitmap加Matrix来缩放
    public Bitmap resizeImage(Bitmap bitmap, int w, int h)
    {
        Bitmap BitmapOrg = bitmap;
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = w;
        int newHeight = h;

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // if you want to rotate the Bitmap
        // matrix.postRotate(45);
        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                height, matrix, true);
        return resizedBitmap;
    }
}
