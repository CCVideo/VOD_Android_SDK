package com.bokecc.sdk.mobile.demo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class VerticalSeekBar extends AppCompatSeekBar {
        public VerticalSeekBar(Context context) {
            super(context);
        }
        public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }
        public VerticalSeekBar(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(h, w, oldh, oldw);
        }

        @Override
        protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(heightMeasureSpec, widthMeasureSpec);
            setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
        }

        protected void onDraw(Canvas c) {
            c.rotate(-90);
            c.translate(-getHeight(), 0);
            super.onDraw(c);
        }

        @Override
        public synchronized void setProgress(int progress) {
            super.setProgress(progress);
            onSizeChanged(getWidth(), getHeight(), 0, 0);  
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (!isEnabled()) {
                return false;
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_UP:
                    setProgress((int) ((int) getMax()- (getMax() * event.getY() / getHeight())));
                    break;
                default:
                    return super.onTouchEvent(event);
            }
            return true;
        }
    }  