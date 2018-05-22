package com.bokecc.sdk.mobile.demo.upload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.sdk.mobile.demo.R;
import com.bokecc.sdk.mobile.demo.model.UploadInfo;
import com.bokecc.sdk.mobile.demo.upload.UploadService.UploadBinder;
import com.bokecc.sdk.mobile.demo.upload.category.Category;
import com.bokecc.sdk.mobile.demo.upload.category.MainCategory;
import com.bokecc.sdk.mobile.demo.util.ConfigUtil;
import com.bokecc.sdk.mobile.demo.util.DataSet;
import com.bokecc.sdk.mobile.upload.Uploader;
import com.bokecc.sdk.mobile.upload.VideoInfo;
import com.bokecc.sdk.mobile.util.HttpUtil;

/**
 * 设置视频信息界面
 * 
 * @author CC视频
 *
 */
public class InputInfoActivity extends Activity implements OnClickListener{
	
	private String filePath = "/storage/sdcard0/1.MP4";
	private RelativeLayout inputLayout;
	private EditText titleEditText;
	private EditText tagsEditText;
	private EditText descEditText;
	private Button uploadButton;
	private String categoryUrl = "https://spark.bokecc.com/api/video/category";
	
	List<String> mainItems = new ArrayList<String>();
	ArrayAdapter<String> mainAdapter;
	
	List<String> subItems = new ArrayList<String>();
	ArrayAdapter<String> subAdapter;
	
	private UploadBinder binder;
	private Intent service;
	private List<Category> list = new ArrayList<Category>();
	private Spinner mainSpinner, subSpinner;
	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case 0:
					Toast.makeText(InputInfoActivity.this, "获取分类失败", Toast.LENGTH_LONG).show();
					break;
				case 1:
					for (Category category: list) {
						mainItems.add(category.getName());
						for (int i=0; i<category.getCount(); i++) {
							subItems.add(category.get(i).getName());
						}
					}
					mainAdapter.notifyDataSetChanged();
					subAdapter.notifyDataSetChanged();
					break;
			}
		}
		
	};
	
	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = (UploadBinder) service;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		service = new Intent(this, UploadService.class);
		inputLayout = new RelativeLayout(this);
		inputLayout.setBackgroundColor(Color.WHITE);
		inputLayout.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
		inputLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		getCategory();
		initView();
		
        bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
		
        String path = getIntent().getStringExtra("filePath");
        if (path != null) {
        	filePath = path;
		}
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
        uploadButton.setOnClickListener(this);
        
        setContentView(inputLayout);
	}
	
	@Override
	protected void onDestroy() {
		
		unbindService(serviceConnection);
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		String title = titleEditText.getText().toString();
		if (title == null || "".equals(title.trim())) {
			Toast.makeText(getApplicationContext(), "请填写视频标题", Toast.LENGTH_SHORT).show();
			return;
		}
		
		String uploadId = UploadInfo.UPLOAD_PRE.concat(System.currentTimeMillis() + "");
		VideoInfo videoInfo = new VideoInfo();
		videoInfo.setTitle(title);
		videoInfo.setTags(tagsEditText.getText().toString());
		videoInfo.setDescription(descEditText.getText().toString());
		videoInfo.setFilePath(filePath);
		
		int mainSelectedItemPosition = mainSpinner.getSelectedItemPosition();
		int subSelectedItemPosition = subSpinner.getSelectedItemPosition();
		String categoryId = "";
		if (mainSelectedItemPosition >=0 && subSelectedItemPosition >= 0) {
			categoryId = list.get(mainSelectedItemPosition).get(subSelectedItemPosition).getId();
			if (categoryId == null) {
				categoryId = "";
			}
		}
		
		videoInfo.setCategoryId(categoryId);
		
		DataSet.addUploadInfo(new UploadInfo(uploadId, videoInfo, Uploader.WAIT, 0, null));
		sendBroadcast(new Intent(ConfigUtil.ACTION_UPLOAD));

		if (binder.isStop()) {
			
			Intent service = new Intent(getApplicationContext(), UploadService.class);
			service.putExtra("title", titleEditText.getText().toString());
			service.putExtra("tag", tagsEditText.getText().toString());
			service.putExtra("desc", descEditText.getText().toString());
			service.putExtra("filePath", filePath);
			service.putExtra("uploadId", uploadId);
			service.putExtra("categoryId", categoryId);
			
			startService(service);
		}
		
		finish();
	}

	private void initView(){
		
		TextView titleText = new TextView(this);
		titleText.setId(ConfigUtil.INPUT_INFO_TITLE_ID);
		titleText.setText("标题");
		titleText.setTextSize(20);
		titleText.setTextColor(Color.BLACK);
		titleText.setPadding(20, 100, 10, 10);
		LayoutParams titleTextLayout = new LayoutParams(150, LayoutParams.WRAP_CONTENT);
		titleTextLayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		inputLayout.addView(titleText, titleTextLayout);
		
		titleEditText = new EditText(this);
		titleEditText.setId(ConfigUtil.INPUT_EDIT_TITLE_ID);
		titleEditText.setPadding(10, 100, 5, 10);
		LayoutParams titleLayout = new LayoutParams(500, LayoutParams.WRAP_CONTENT);
		titleLayout.addRule(RelativeLayout.RIGHT_OF, ConfigUtil.INPUT_INFO_TITLE_ID);
		inputLayout.addView(titleEditText, titleLayout);
		
		TextView tagsTextView = new TextView(this);
		tagsTextView.setId(ConfigUtil.INPUT_INFO_TAGS_ID);
		tagsTextView.setText("标签");
		tagsTextView.setTextSize(20);
		tagsTextView.setTextColor(Color.BLACK);
		tagsTextView.setPadding(20, 50, 10, 10);
		LayoutParams tagsTextLayout = new LayoutParams(150, LayoutParams.WRAP_CONTENT);
		tagsTextLayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		tagsTextLayout.addRule(RelativeLayout.BELOW, ConfigUtil.INPUT_INFO_TITLE_ID);
		inputLayout.addView(tagsTextView, tagsTextLayout);
		
		tagsEditText = new EditText(this);
		tagsEditText.setId(ConfigUtil.INPUT_EDIT_TAGS_ID);
		tagsEditText.setPadding(10, 50, 5, 10);
		LayoutParams tagsLayout = new LayoutParams(500, LayoutParams.WRAP_CONTENT);
		tagsLayout.addRule(RelativeLayout.BELOW, ConfigUtil.INPUT_INFO_TITLE_ID);
		tagsLayout.addRule(RelativeLayout.RIGHT_OF, ConfigUtil.INPUT_INFO_TAGS_ID);
		inputLayout.addView(tagsEditText, tagsLayout);
		
		TextView descTextView = new TextView(this);
		descTextView.setId(ConfigUtil.INPUT_INFO_DESC_ID);
		descTextView.setText("简介");
		descTextView.setTextSize(20);
		descTextView.setTextColor(Color.BLACK);
		descTextView.setPadding(20, 50, 10, 10);
		LayoutParams descTextLayout = new LayoutParams(150, LayoutParams.WRAP_CONTENT);
		descTextLayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		descTextLayout.addRule(RelativeLayout.BELOW, ConfigUtil.INPUT_INFO_TAGS_ID);
		inputLayout.addView(descTextView, descTextLayout);
		
		descEditText = new EditText(this);
		descEditText.setId(ConfigUtil.INPUT_EDIT_DESC_ID);
		descEditText.setPadding(10, 50, 5, 10);
		LayoutParams descLayout = new LayoutParams(500, LayoutParams.WRAP_CONTENT);
		descLayout.addRule(RelativeLayout.BELOW, ConfigUtil.INPUT_INFO_TAGS_ID);
		descLayout.addRule(RelativeLayout.RIGHT_OF, ConfigUtil.INPUT_INFO_DESC_ID);
		inputLayout.addView(descEditText, descLayout);
		
		mainSpinner = new Spinner(this);
		mainSpinner.setId(ConfigUtil.SPINNER_MAIN_ID);
		mainSpinner.setPadding(20, 50, 10, 10);
		
		LayoutParams spinnerMainLayout = new LayoutParams(650, LayoutParams.WRAP_CONTENT);
		spinnerMainLayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		spinnerMainLayout.addRule(RelativeLayout.BELOW, ConfigUtil.INPUT_EDIT_DESC_ID);
		mainAdapter = new ArrayAdapter<String>(this, R.layout.spinner_view, mainItems);
		mainSpinner.setAdapter(mainAdapter);
		inputLayout.addView(mainSpinner, spinnerMainLayout);
		
		subSpinner = new Spinner(this);
		subSpinner.setId(ConfigUtil.SPINNER_SUB_ID);
		subSpinner.setPadding(20, 50, 10, 10);
		LayoutParams spinnerSubLayout = new LayoutParams(650, LayoutParams.WRAP_CONTENT);
		spinnerSubLayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		spinnerSubLayout.addRule(RelativeLayout.BELOW, ConfigUtil.SPINNER_MAIN_ID);
		subAdapter = new ArrayAdapter<String>(this, R.layout.spinner_view, subItems);
		subSpinner.setAdapter(subAdapter);
		inputLayout.addView(subSpinner, spinnerSubLayout);
		
		uploadButton = new Button(this);
		uploadButton.setText("上传");
		uploadButton.setPadding(10, 0, 5, 10);
		LayoutParams uploadButtonLayout = new LayoutParams(500, LayoutParams.WRAP_CONTENT);
		uploadButtonLayout.addRule(RelativeLayout.BELOW, ConfigUtil.SPINNER_SUB_ID);
		uploadButtonLayout.addRule(RelativeLayout.ALIGN_RIGHT, ConfigUtil.SPINNER_SUB_ID);
		uploadButtonLayout.setMargins(0, 100, 0, 0);
		inputLayout.addView(uploadButton, uploadButtonLayout);
		
		mainSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Category category = list.get(position);
				subItems.clear();
				for (int i=0; i<category.getCount(); i++) {
					subItems.add(category.get(i).getName());
				}
				subAdapter.notifyDataSetChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
		});
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void getCategory() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Map<String, String> params = new HashMap<String, String>();
				params.put("userid", ConfigUtil.USERID);
				params.put("format", "json");
				try {
					String result = HttpUtil.getResult(categoryUrl, params, ConfigUtil.API_KEY, false);
					getCategoryInfo(result);
				} catch (JSONException e) {
					handler.sendEmptyMessage(0);
				}
			}
		}).start();
	}
	
	private void getCategoryInfo(String result) {
		try {
			JSONArray categoryArray = new JSONObject(result).getJSONObject("video").getJSONArray("category");
			for (int i=0; i<categoryArray.length(); i++) {
				list.add(new MainCategory(categoryArray.getJSONObject(i)));
			}
			handler.sendEmptyMessage(1);
		} catch (JSONException e) {
			handler.sendEmptyMessage(0);
		}
	}
	
}
