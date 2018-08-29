package com.bokecc.sdk.mobile.demo.play.qa;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.widget.TextView;

import com.bokecc.sdk.mobile.demo.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.lang.reflect.Field;

/**
 * Created by Administrator on 2018/5/10.
 */

public class UrlImageSpan extends ImageSpan {

    private String url;
    private TextView tv;
    private boolean picShowed;

    public UrlImageSpan(Context context, String url, TextView tv) {
        super(context, R.drawable.iv_load_img_fail);
        this.url = url;
        this.tv = tv;
    }

    @Override
    public Drawable getDrawable() {
        if (!picShowed) {
            Glide.with(tv.getContext()).load(url).asBitmap().into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    if (resource != null) {
                        Bitmap zoom = null;
                        int targetWidth = 216;
                        double controlRatio = 0.3;
                        Resources resources = tv.getContext().getResources();
                        if (resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            targetWidth = (int) (resources.getDisplayMetrics().heightPixels * controlRatio);
                        } else {
                            targetWidth = (int) (resources.getDisplayMetrics().widthPixels * controlRatio);
                        }
                        int width = resource.getWidth();
                        if (width > targetWidth) {
                            zoom = zoom(resource, targetWidth);
                        } else {
                            zoom = resource;
                        }

                        BitmapDrawable b = new BitmapDrawable(resources, zoom);

                        b.setBounds(0, 0, b.getIntrinsicWidth(), b.getIntrinsicHeight());
                        Field mDrawable;
                        Field mDrawableRef;
                        try {
                            mDrawable = ImageSpan.class.getDeclaredField("mDrawable");
                            mDrawable.setAccessible(true);
                            mDrawable.set(UrlImageSpan.this, b);

                            mDrawableRef = DynamicDrawableSpan.class.getDeclaredField("mDrawableRef");
                            mDrawableRef.setAccessible(true);
                            mDrawableRef.set(UrlImageSpan.this, null);

                            picShowed = true;
                            tv.setText(tv.getText());
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        return super.

                getDrawable();

    }

    /**
     * 按宽度缩放图片
     *
     * @param bmp  需要缩放的图片源
     * @param newW 需要缩放成的图片宽度
     * @return 缩放后的图片
     */
    public static Bitmap zoom(@NonNull Bitmap bmp, int newW) {

        // 获得图片的宽高
        int width = bmp.getWidth();
        int height = bmp.getHeight();

        // 计算缩放比例
        float scale = ((float) newW) / width;

        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);

        return newbm;
    }

}
