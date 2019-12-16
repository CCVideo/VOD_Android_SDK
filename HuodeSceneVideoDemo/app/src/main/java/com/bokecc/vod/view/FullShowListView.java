package com.bokecc.vod.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class FullShowListView extends ListView {
    public FullShowListView(Context context) {
        super(context);
    }

    public FullShowListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FullShowListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int spec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, spec);
    }
}
