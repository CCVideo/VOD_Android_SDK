package com.bokecc.sdk.mobile.demo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bokecc.sdk.mobile.demo.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author cc视频
 *         <p>
 *         自定义进度条
 */

public class HotspotSeekBar extends View {

    public HotspotSeekBar(Context context) {
        this(context, null);
    }

    public HotspotSeekBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    Context mContext;
    WindowManager mWindowManager;

    public HotspotSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        initPaint();
    }

    boolean isHotspotShown = false;

    /**
     * 设置热点是否显示，默认为不显示热点
     *
     * @param isHotspotShown
     */
    public void setHotspotShown(boolean isHotspotShown) {
        this.isHotspotShown = isHotspotShown;
        invalidate();
    }

    public void setSecondaryProgress(int progress) {
        this.secondProgress = progress;
    }

    public int getProgress() {
        return progress;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMax() {
        return max;
    }

    private boolean isTouch = false;
    private float oldThumbPosition;
    private float currentThumbStart;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        oldThumbPosition = thumbStart;

        currentThumbStart = event.getX();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isHotspotShown && isHotSpotClicked()) {
                    showPopupWindow();
                    return false;
                } else {
                    isTouch = true;

                    if (onSeekBarChangeListener != null) {
                        onSeekBarChangeListener.onStartTrackingTouch(this);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isTouch) {
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isTouch) {

                    isTouch = false;

                    if (onSeekBarChangeListener != null) {

                        float progressPercent = (thumbStart - getHeight() / 2) / (getWidth() - getHeight());
                        if (progressPercent > 1.0f) {
                            progressPercent = 1.0f;
                        }

                        onSeekBarChangeListener.onStopTrackingTouch(this, progressPercent);
                    }

                    break;
                } else {
                    return false;
                }

            default:
                return false;
        }

        thumbStart = currentThumbStart;
        processThumbStart();
        invalidate();

        return true;
    }

    private PopupWindow popupWindow;
    private TextView hotspotDescTextView;
    private ImageView arrowView;

    private void showPopupWindow() {
        if (popupWindow == null) {
            initPopupWindow();
            return;
        }

        hotspotDescTextView.setText(getDescStr());
        popupPopupWindow();
    }

    private void initPopupWindow() {
        popupWindow = new PopupWindow(mContext);
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        final View view = LayoutInflater.from(mContext).inflate(R.layout.indicator_popupwindow, null);
        hotspotDescTextView = view.findViewById(R.id.hotspot_desc);
        hotspotDescTextView.setText(getDescStr());

        arrowView = view.findViewById(R.id.arrow_indicator);

        popupWindow.setContentView(view);
//        mPopupWindow.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.indicator_bg));
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        popupWindow.setOutsideTouchable(true);
//        mPopupWindow.setFocusable(false);
        popupWindow.setTouchInterceptor(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();

                if (event.getAction() != MotionEvent.ACTION_OUTSIDE) {
                    if (onIndicatorTouchListener != null) {
                        onIndicatorTouchListener.onIndicatorTouch(clickedHotSpot.getHotspotPosition());
                    }
                }
                return false;
            }
        });

        popupPopupWindow();
    }

    private String getDescStr() {
        return getMinuteSecondStr(clickedHotSpot.getHotspotPosition().intValue()) + "   " + clickedHotSpot.getHotspotDesc();
    }

    private void popupPopupWindow() {
        int[] position = new int[2];
        getLocationOnScreen(position);

        int rawX = position[0] + (int) currentThumbStart;

        popupWindow.getContentView().measure(0, 0);

        int measureWidth = popupWindow.getContentView().getMeasuredWidth();
        int measureHeight = popupWindow.getContentView().getMeasuredHeight();

        popupWindow.showAsDropDown(this, (int) currentThumbStart - measureWidth / 2,
                (measureHeight + getHeight() + 15 + 10) * -1);

        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) arrowView.getLayoutParams();
        if ((rawX - measureWidth / 2) < 0) {
            int lefMargin = -1 * (measureWidth / 2 - rawX);
            layoutParams.setMargins(lefMargin, 0, 0, 0);
        } else if ((rawX + measureWidth / 2) > getWindowWidth()) {
            int lefMargin = rawX - (getWindowWidth() - measureWidth / 2);
            layoutParams.setMargins(lefMargin, 0, 0, 0);
        } else {
            layoutParams.setMargins(0, 0, 0, 0);
        }

        arrowView.requestLayout();
    }

    /**
     * 提示框是否弹出
     *
     * @return
     */
    public boolean isPopupWindowShow() {
        if (popupWindow == null) {
            return false;
        } else {
            return popupWindow.isShowing();
        }
    }

    /**
     * 隐藏弹出来的提示框
     */
    public void dismissPopupWindow() {
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
    }

    private HotSpot clickedHotSpot;

    private boolean isHotSpotClicked() {
        for (HotSpot hotSpot : hotspotList) {
            float mPositionPercent = hotSpot.getHotspotPercent();
            float centerPosition = (getWidth() - getHeight()) * mPositionPercent + getHeight() / 2;

            boolean isOnHotPoint = (currentThumbStart > centerPosition - hotspotWidth * 3) && currentThumbStart < (centerPosition + hotspotWidth * 3);
//            boolean isOldThumbOn = (oldThumbPosition > centerPosition - hotspotWidth * 3) && oldThumbPosition < (centerPosition + hotspotWidth * 3);

            if (isOnHotPoint) {
//                if (isOldThumbOn) {
//                    return false;
//                } else {
                clickedHotSpot = hotSpot;
                this.currentThumbStart = centerPosition;
                return true;
//                }
            }
        }

        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawProgress(canvas);
        drawHotspot(canvas);
        drawThumb(canvas);

        canvas.save();
        canvas.restore();
    }

    private void processThumbStart() {
        int progressRightSide = getWidth() - getHeight() / 2;
        int progressLeftSide = getHeight() / 2;
        if (thumbStart > progressRightSide) {
            thumbStart = progressRightSide;
        } else if (thumbStart < progressLeftSide) {
            thumbStart = progressLeftSide;
        }
    }

    private void drawProgress(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        float heightBaseLine = getHeightBaseLine(height);

        float leftPosition = 0.0f + height / 2;
        float topPosition = 0.0f + heightBaseLine;
        float bottomPosition = height - heightBaseLine;

        float secondaryProgressRightPosition = (float) (width - height) * secondProgress / 100 + height / 2;

        canvas.drawRect(leftPosition, topPosition, width - height, bottomPosition, backgroundPaint);
        canvas.drawRect(leftPosition, topPosition, secondaryProgressRightPosition, bottomPosition, secondProgressPaint);
        canvas.drawRect(leftPosition, topPosition, thumbStart, bottomPosition, progressPaint);
    }

    float thumbStart = 0.0f;

    private void drawThumb(Canvas canvas) {

        if (thumbStart <= 0.0f) {
            thumbStart = getLayoutParams().height / 2; //TODO 这里导致了设置layout_height的时候，不能为wrap_content
        }

        int height = canvas.getHeight();
        if (isTouch) {
            // 绘制两个圆，这样展示出来的就是有边框的圆
            canvas.drawCircle(thumbStart, height / 2, height / 2, progressPaint);
            canvas.drawCircle(thumbStart, height / 2, height / 2 - 3, thumbPaint);
        } else {
            canvas.drawCircle(thumbStart, height / 2, height * 2 / 5, progressPaint);
            canvas.drawCircle(thumbStart, height / 2, height * 2 / 5 - 3, thumbPaint);
        }
    }

    float hotspotWidth = 8.0f;

    private void drawHotspot(Canvas canvas) {
        if (!isHotspotShown) {
            return;
        }

        int height = canvas.getHeight();
        int width = canvas.getWidth();

        float heightBaseLine = getHeightBaseLine(height);
        float topPosition = 0.0f + heightBaseLine;
        float bottomPosition = height - heightBaseLine;


        if (hotspotList.size() > 0) {
            for (HotSpot hotSpot : hotspotList) {

                float mPositionPercent = hotSpot.getHotspotPercent();
                float leftPosition = (width - height) * mPositionPercent + height / 2 - hotspotWidth;

                float rightPosition = (width - height) * mPositionPercent + height / 2 + hotspotWidth;
                float roundRectRadius = height * 3 / 2;

                RectF mRectF = new RectF(leftPosition, topPosition, rightPosition, bottomPosition);
                canvas.drawRoundRect(mRectF, roundRectRadius, roundRectRadius, hotSpotPaint);
            }
        }
    }

    private float getHeightBaseLine(int height) {
        return (float) height * 2 / 5;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        MeasureSpec.getMode(widthMeasureSpec);
        MeasureSpec.getSize(widthMeasureSpec);

    }

    /**
     * 设置当前的进度
     *
     * @param progress 当前进度
     * @param duration 总时长
     */
    public void setProgress(int progress, int duration) {
        if (isTouch) {
            return;
        }

        this.progress = progress;
        this.max = duration;

        thumbStart = (getWidth() - getHeight()) * (float) progress / duration + getHeight() / 2;

        postInvalidate();
    }

    public interface OnSeekBarChangeListener {
        /**
         * 开始滑动
         *
         * @param seekBar
         */
        void onStartTrackingTouch(HotspotSeekBar seekBar);

        /**
         * 结束滑动
         *
         * @param seekBar
         * @param trackStopPercent 结束滑动的百分比位置
         */
        void onStopTrackingTouch(HotspotSeekBar seekBar,
                                 float trackStopPercent);
    }

    private OnSeekBarChangeListener onSeekBarChangeListener;

    /**
     * 设置seekbar的滑动监听器
     *
     * @param onSeekBarChangeListener
     */
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener onSeekBarChangeListener) {
        this.onSeekBarChangeListener = onSeekBarChangeListener;
    }

    public interface OnIndicatorTouchListener {
        /**
         * 回调当前点击的指示框对应的播放器的位置，时间单位为s
         *
         * @param currentPosition
         */
        void onIndicatorTouch(int currentPosition);
    }

    OnIndicatorTouchListener onIndicatorTouchListener;

    /**
     * 设置点击指示框的监听
     *
     * @param onIndicatorTouchListener
     */
    public void setOnIndicatorTouchListener(OnIndicatorTouchListener onIndicatorTouchListener) {
        this.onIndicatorTouchListener = onIndicatorTouchListener;
    }

    List<HotSpot> hotspotList = new ArrayList<>();

    /**
     * 设置热点位置信息
     *
     * @param videoMarks 热点信息
     * @param duration   播放器总时长
     */
    public void setHotSpotPosition(TreeMap<Integer, String> videoMarks, float duration) {
        hotspotList.clear();

        for (Map.Entry<Integer, String> entry : videoMarks.entrySet()) {
            float hotspotPercent = entry.getKey().floatValue() / duration;
            hotspotList.add(new HotSpot(entry.getKey(), entry.getValue(), hotspotPercent));
        }

        postInvalidate();
    }

    /**
     * 清除已有的打点信息
     */
    public void clearHotSpots() {
        hotspotList.clear();
    }

    // 格式化时间为分秒的形式
    private String getMinuteSecondStr(int time) {
        StringBuilder sb = new StringBuilder();
        int minute = time / 60;
        int second = time % 60;
        sb.append(addZeroOnTime(minute))
                .append(":")
                .append(addZeroOnTime(second));

        return sb.toString();
    }

    // 格式化时间，十位为0的时候，十位补0。如0，则返回"00"
    private String addZeroOnTime(int time) {
        if (time < 10) {
            return new StringBuilder("0").append(time).toString();
        } else {
            return new StringBuilder().append(time).toString();
        }
    }

    // 获取当前的屏幕宽度
    private int getWindowWidth() {
        return mWindowManager.getDefaultDisplay().getWidth();
    }

    private Paint backgroundPaint, progressPaint, secondProgressPaint, thumbPaint, hotSpotPaint;
    int progress = 0;
    int secondProgress = 0, max = 100;

    private void initPaint() {
        backgroundPaint = getPaint(SeekBarColorConfig.BACKGROUND_COLOR);
        secondProgressPaint = getPaint(SeekBarColorConfig.SECOND_PROGRESS_COLOR);
        progressPaint = getPaint(SeekBarColorConfig.PLAY_PROGRESS_COLOR);
        thumbPaint = getPaint(SeekBarColorConfig.THUMB_COLOR);
        hotSpotPaint = getPaint(SeekBarColorConfig.HOTSPOT_COLOR);
    }

    private Paint getPaint(int color) {
        Paint mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(color);
        return mPaint;
    }

    // 热点类
    private class HotSpot {
        private Integer hotspotPosition;
        private String hotspotDesc;
        private float hotspotPercent; //热点的百分比位置

        public HotSpot(Integer hotspotPosition, String markPosition, float timePercent) {
            this.hotspotPosition = hotspotPosition;
            this.hotspotDesc = markPosition;
            this.hotspotPercent = timePercent;
        }

        public Integer getHotspotPosition() {
            return hotspotPosition;
        }

        public String getHotspotDesc() {
            return hotspotDesc;
        }

        public float getHotspotPercent() {
            return hotspotPercent;
        }
    }

}