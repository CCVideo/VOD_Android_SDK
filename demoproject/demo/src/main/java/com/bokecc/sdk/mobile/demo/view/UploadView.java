package com.bokecc.sdk.mobile.demo.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bokecc.sdk.mobile.demo.R;
import com.bokecc.sdk.mobile.demo.util.ParamsUtil;

@SuppressLint("NewApi")
public class UploadView extends RelativeLayout {
	
	public final static int NOTIFY_DELETE = -1000000;

	private TextView idView;

	private ImageView imageView;

	private TextView titleView;
	
	private TextView statusTextView;

	private TextView progressTextView;

	private ProgressBar progressBar;
	
	private final int VIDEOIMAGE_ID = 1000000;
	private final int TITLEVIEW_ID = 1000001;
	private final int STATUSVIEW_ID = 1000002;
	private final int PROGRESSTEXT_ID = 1000003;
	private final int PROGRESSBAR_ID = 1000004;

	public UploadView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public UploadView(Context context, String uploadId, Bitmap bm, String title, String statusText, String pText, int progress) {
		super(context);
		
		idView = new TextView(context);
		idView.setVisibility(View.GONE);
		idView.setText(uploadId + "");

		imageView = new ImageView(context);
		imageView.setImageBitmap(bm);
		imageView.setBackgroundColor(0xFF666666);
		imageView.setId(VIDEOIMAGE_ID);
		
		titleView = new TextView(context);
		titleView.setText(title);
		titleView.setTextColor(0xFF000000);
		titleView.setId(TITLEVIEW_ID);
		titleView.setPaddingRelative(5, 5, 0, 0);
		
		statusTextView = new TextView(context);
		statusTextView.setText(statusText);
		statusTextView.setTextColor(0xFF000000);
		statusTextView.setId(STATUSVIEW_ID);
		statusTextView.setPaddingRelative(5, 5, 0, 0);
		statusTextView.setTextColor(Color.GRAY);
		
		progressTextView = new TextView(context);
		progressTextView.setText(pText);
		progressTextView.setTextColor(0xFF000000);
		progressTextView.setId(PROGRESSTEXT_ID);
		progressTextView.setPaddingRelative(5, 5, 0, 0);
		progressTextView.setTextColor(Color.GRAY);
		
		progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
		progressBar.setMax(100);
		progressBar.setIndeterminate(false);
        progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.progressbar));
        progressBar.setId(PROGRESSBAR_ID);
        progressBar.setPaddingRelative(5, 0, 0, 0);
        progressBar.setProgress(progress);
        
        /**
         *  
         *  + + + +  title
         *  + img +  0M / 0M
         *  +     +  下载中
         *  + + + +  ********progress**************
         *  
         * */
        //左侧
        LayoutParams imageLayoutParams = new LayoutParams(ParamsUtil.dpToPx(context, 75), ParamsUtil.dpToPx(context, 75));
		imageLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		
		//位于图片右侧
		LayoutParams titleLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		titleLayoutParams.addRule(RelativeLayout.RIGHT_OF, VIDEOIMAGE_ID);
		
		//位于标题下方
		LayoutParams statusLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		statusLayoutParams.addRule(RelativeLayout.BELOW, TITLEVIEW_ID);
		statusLayoutParams.addRule(RelativeLayout.RIGHT_OF, VIDEOIMAGE_ID);
		
		//位于状态下方
		LayoutParams progressTextParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		progressTextParams.addRule(RelativeLayout.BELOW, STATUSVIEW_ID);
		progressTextParams.addRule(RelativeLayout.RIGHT_OF, VIDEOIMAGE_ID);
		
		//位于图片左侧，进度文本下方
        LayoutParams progressBarLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, ParamsUtil.dpToPx(context, 7));
        progressBarLayoutParams.addRule(RelativeLayout.RIGHT_OF, VIDEOIMAGE_ID);
        progressBarLayoutParams.addRule(RelativeLayout.BELOW, PROGRESSTEXT_ID);
        progressBarLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        
		addView(imageView, imageLayoutParams);
		addView(titleView, titleLayoutParams);
		addView(statusTextView, statusLayoutParams);
		addView(progressTextView, progressTextParams);
		addView(progressBar, progressBarLayoutParams);
		setPadding(5, 5, 5, 5);

	}
	
	public String getUploadId(){
		return idView.getText() + "";
	}

	public void setProgressText(String text, Context context) {
		progressTextView.setText(text);
	}

	public void setProgress(int progress) {
		progressBar.setProgress(progress);
	}

	public void setProgressText(String text){
		progressTextView.setText(text);
	}
	
}
