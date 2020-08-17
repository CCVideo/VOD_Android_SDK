package com.bokecc.vod.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * 环形进度条
 */
public class CircleProgressBar extends View {

	private Context mContext;
	private RectF mRectF;
	private Paint mPaint;
	private final int mCircleLineStrokeWidth = 4;
	
	public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        mRectF = new RectF();
        mPaint = new Paint();
        
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.rgb(60, 132, 189));
        mPaint.setStrokeWidth(mCircleLineStrokeWidth);
        mPaint.setStyle(Style.STROKE);
        
    }
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		int width = canvas.getWidth();
        int height = canvas.getHeight();
        int radios = Math.min(width, height);
        
		mRectF.left = width /2 - radios / 2 + mCircleLineStrokeWidth / 2; // 左上角x
        mRectF.top = 0  + mCircleLineStrokeWidth / 2; // 左上角y
        mRectF.right = width /2 + radios / 2 - mCircleLineStrokeWidth / 2; // 右下角x
        mRectF.bottom = height - mCircleLineStrokeWidth / 2; // 右下角y
		
        canvas.drawArc(mRectF, -90, (progress / max) * 360, false, mPaint);
	}
	
	
	private float max = 2.0f;
	public void setMax(float max) {
		this.max = max;
	}
	
	private float progress;
	public void setProgress(float progress) {
		this.progress = progress;
		if (progress > max) {
			progress = max;
		}
		this.invalidate();
	}
	
}
