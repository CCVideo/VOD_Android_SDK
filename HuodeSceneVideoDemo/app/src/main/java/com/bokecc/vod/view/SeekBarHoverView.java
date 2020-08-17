package com.bokecc.vod.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

/**
 * vr播放进度条
 */
public class SeekBarHoverView extends SeekBar {
    public SeekBarHoverView(Context context) {
        super(context);
    }

    public SeekBarHoverView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SeekBarHoverView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private float lastX;
    private boolean isSeek = false;

    @Override
    public boolean onHoverEvent(MotionEvent event) {
        super.onHoverEvent(event);

        if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
            isSeek = false;
        }

        if (Math.abs(event.getX() - lastX) < 10) {
            if (event.getEventTime() - event.getDownTime() > 950 && !isSeek) {

                if (listener != null) {
                    listener.onHover(lastX);
                }

                isSeek = true;
            }

        } else {
            lastX = event.getX();
        }

        return true;
    }

    private SeekBarHoverEventListener listener;

    public void setOnHoverEventListener(SeekBarHoverEventListener listener) {
        this.listener = listener;
    }

    public static interface SeekBarHoverEventListener {
        public void onHover(float x);

    }
}
