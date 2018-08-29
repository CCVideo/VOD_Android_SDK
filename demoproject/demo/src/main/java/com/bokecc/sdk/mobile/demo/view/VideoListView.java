package com.bokecc.sdk.mobile.demo.view;

import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bokecc.sdk.mobile.demo.util.ParamsUtil;

public class VideoListView extends RelativeLayout{
	
	protected TextView videoView;
	
	protected ImageView imageView;
	
	protected final int VIDEOIDVIEW_ID = 5000001;
	
	public VideoListView(Context context, String text, int resId) {
		super(context);
	
		videoView = new TextView(context);
		videoView.setText(text);
		videoView.setId(VIDEOIDVIEW_ID);
		videoView.setTextSize(15);
		videoView.setTextColor(Color.BLACK);
		videoView.setPadding(0, 10, 0, 0);
		videoView.setSingleLine();
		
		imageView = new ImageView(context);
		imageView.setImageResource(resId);
		imageView.setPadding(0, 10, 10, 0);
		
		LayoutParams VideoLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		VideoLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		addView(videoView, VideoLayoutParams);
		
		LayoutParams imageLayoutParams = new LayoutParams(ParamsUtil.dpToPx(context, 25), ParamsUtil.dpToPx(context, 25));
		imageLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		imageLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		addView(imageView, imageLayoutParams);
		
		setMinimumHeight(ParamsUtil.dpToPx(context, 48));
		setPadding(10, 10, 10, 10);
	}
	
}
