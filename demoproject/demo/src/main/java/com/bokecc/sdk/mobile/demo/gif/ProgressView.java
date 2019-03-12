package com.bokecc.sdk.mobile.demo.gif;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;


public class ProgressView extends View {

	/** 进度条 */
	private Paint mProgressActivePaint;

	private Paint mProgressInactivePaint;
	/** 闪 */
	private Paint mActivePaint;
	/** 三秒 */
	private Paint mThreePaint;

	private boolean mStop;
	private boolean mActiveState;
	private ProgressObject mProgressObject;
	/** 最长时长 */
	private int mMaxDuration, mVLineWidth;

	public ProgressView(Context paramContext) {
		super(paramContext);
		init();
	}

	public ProgressView(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
		init();
	}

	public ProgressView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
		init();
	}

	private void init() {

		mProgressActivePaint = new Paint();
		mProgressInactivePaint = new Paint();

		mActivePaint = new Paint();
		mThreePaint = new Paint();
		mVLineWidth = dipToPX(getContext(), 2);

		mProgressActivePaint.setColor(0xFF5DF02F);
		mProgressActivePaint.setStyle(Paint.Style.FILL);

		mProgressInactivePaint.setColor(0xFFFF6633);
		mProgressInactivePaint.setStyle(Paint.Style.FILL);

		mActivePaint.setColor(getResources().getColor(android.R.color.white));
		mActivePaint.setStyle(Paint.Style.FILL);

		mThreePaint.setColor(0xCCFFFF00);
		mThreePaint.setStyle(Paint.Style.FILL);

	}

	public int dipToPX(final Context ctx, float dip) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, ctx.getResources().getDisplayMetrics());
	}

	/** 闪动 */
	private final static int HANDLER_INVALIDATE_ACTIVE = 0;

	private Handler mHandler = new Handler() {
		@Override
		public void dispatchMessage(Message msg) {
			switch (msg.what) {
			case HANDLER_INVALIDATE_ACTIVE:
				invalidate();
				mActiveState = !mActiveState;
				if (!mStop)
					sendEmptyMessageDelayed(0, 200);
				break;
			}
			super.dispatchMessage(msg);
		}
	};

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		final int width = getMeasuredWidth(), height = getMeasuredHeight();
		int left = 0, right = 0, duration = 0;
		int currentDuration = 0;
		if (mProgressObject != null) {
			currentDuration = mProgressObject.getDuration();
			right = left + (int) (currentDuration * 1.0F / mMaxDuration * width);

			if (currentDuration < mRecordTimeMin) {
				canvas.drawRect(left, 0.0F, right, height, mProgressInactivePaint);
			} else {
				canvas.drawRect(left, 0.0F, right, height, mProgressActivePaint);
			}
		}

		if (currentDuration < mRecordTimeMin) {
			left = (int) ((mRecordTimeMin * 1.0f )/ mMaxDuration * width);
			canvas.drawRect(left, 0.0F, left + mVLineWidth, height, mThreePaint);
		}

		if (mActiveState) {
			if (right + 8 >= width) {
				right = width - 8;
			}

			canvas.drawRect(right, 0.0F, right + 8, getMeasuredHeight(), mActivePaint);
		}
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		mStop = false;
		mHandler.sendEmptyMessage(HANDLER_INVALIDATE_ACTIVE);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mStop = true;
		mHandler.removeMessages(HANDLER_INVALIDATE_ACTIVE);
	}

	public void setData(ProgressObject mMediaObject) {
		this.mProgressObject = mMediaObject;
	}

	public void setMaxDuration(int duration) {
		this.mMaxDuration = duration;
	}

	int mRecordTimeMin;
	public void setMinTime(int recordTimeMin) {
		this.mRecordTimeMin = recordTimeMin;
	}

}