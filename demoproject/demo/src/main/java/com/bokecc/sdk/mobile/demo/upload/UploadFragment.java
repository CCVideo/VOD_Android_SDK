package com.bokecc.sdk.mobile.demo.upload;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.bokecc.sdk.mobile.demo.R;
import com.bokecc.sdk.mobile.demo.adapter.UploadViewAdapter;
import com.bokecc.sdk.mobile.demo.model.UploadInfo;
import com.bokecc.sdk.mobile.demo.upload.UploadService.UploadBinder;
import com.bokecc.sdk.mobile.demo.util.ConfigUtil;
import com.bokecc.sdk.mobile.demo.util.DataSet;
import com.bokecc.sdk.mobile.demo.util.ParamsUtil;
import com.bokecc.sdk.mobile.demo.view.UploadView;
import com.bokecc.sdk.mobile.exception.ErrorCode;
import com.bokecc.sdk.mobile.upload.Uploader;
import com.bokecc.sdk.mobile.upload.VideoInfo;

/**
 *
 * 上传标签页，用于展示视频上传进度、文件截图等信息
 * 
 * @author CC视频
 *
 */
public class UploadFragment extends Fragment {

	private Context context;
	private FragmentActivity activity;

	private Button uploadButton;
	private ListView uploadListView;
	private UploadViewAdapter uploadAdapter;
	private List<UploadInfo> uploadInfos;
	private UploadBinder binder;
	private Intent service;
	private ServiceConnection serviceConnection;
	private UploadReceiver receiver;
	
	private boolean isBind;
	private Timer timer = new Timer();
	private String currentUploadId;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		activity = getActivity();
		context = activity.getApplicationContext();
		receiver = new UploadReceiver();
		activity.registerReceiver(receiver, new IntentFilter(ConfigUtil.ACTION_UPLOAD));
		service = new Intent(context, UploadService.class);
		
		binderService();
		
		RelativeLayout view = new RelativeLayout(context);
		view.setBackgroundColor(Color.WHITE);
		LayoutParams uploadLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		uploadListView = new ListView(context);
		uploadListView.setDivider(getResources().getDrawable(R.drawable.line));
		view.addView(uploadListView, uploadLayoutParams);

		uploadListView.setOnItemClickListener(onItemClickListener);
		uploadListView.setOnCreateContextMenuListener(onCreateContextMenuListener);
		
		initUploadList();
		
		LayoutParams uploadButtonLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		uploadButtonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		uploadButtonLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		
		uploadButton = new Button(context);
		view.addView(uploadButton, uploadButtonLayoutParams);
		uploadButton.setText("上传");
		uploadButton.setTextColor(0xFFFFFFFF);
		uploadButton.setOnClickListener(uploadOnClickListener);
		
		timer.schedule(timerTask, 0, 1000);
		return view;
	}
	
	private void binderService() {
		serviceConnection = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.i("service disconnected", name + "");
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				binder = (UploadBinder) service;
			}
		};
		
		activity.bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
		isBind = true;
	}

	private void initUploadList() {
		uploadInfos = DataSet.getUploadInfos();
		
		for (UploadInfo uploadInfo: uploadInfos) {
			if ((uploadInfo.getStatus() == Uploader.UPLOAD) && (binder == null || binder.isStop())){
				startUploadService(uploadInfo);
				currentUploadId = uploadInfo.getUploadId();
				break;
			}
		}
		
		uploadAdapter = new UploadViewAdapter(context, uploadInfos);
		uploadListView.setAdapter(uploadAdapter);
	}
	
	OnItemClickListener onItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			UploadView uploadView = (UploadView)view;
			String uploadId = uploadView.getUploadId();
			if (binder.isStop()) {
				UploadInfo uploadInfo = DataSet.getUploadInfo(uploadId);
				if (uploadInfo != null && uploadInfo.getStatus() != Uploader.FINISH) {
					startUploadService(uploadInfo);
				}
				
				currentUploadId = uploadId;
				
			} else if (uploadId.equals(currentUploadId)) {
				
				switch (binder.getUploaderStatus()) {
				case Uploader.UPLOAD:
					binder.pause();
					break;
				case Uploader.PAUSE:
					binder.upload();
					break;
				}
			}
		}

	};
	
	OnCreateContextMenuListener onCreateContextMenuListener = new OnCreateContextMenuListener() {
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
			menu.setHeaderTitle("操作");
			menu.add(0, 0, 0, "删除");
		}
	};	
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int selectedPosition = ((AdapterContextMenuInfo) item.getMenuInfo()).position;//获取点击了第几行
		UploadInfo uploadInfo = (UploadInfo) uploadAdapter.getItem(selectedPosition);
		String uploadId = uploadInfo.getUploadId();
		
		//通知service取消上传
		if (!binder.isStop() && uploadId.equals(currentUploadId)) {
			binder.cancle();
		}
		
		//删除记录
		DataSet.removeUploadInfo(uploadId);
		
		initUploadList();
		uploadAdapter.notifyDataSetChanged();
		uploadListView.invalidate();

		return super.onContextItemSelected(item);
	}
	
	OnClickListener uploadOnClickListener = new OnClickListener() {
		@SuppressLint("InlinedApi") 
		@Override
		public void onClick(View v) {
			Toast.makeText(context, "正在搜索视频文件",  Toast.LENGTH_SHORT).show();
			Intent intent = null;
			if(android.os.Build.VERSION.SDK_INT < 19){
				intent = new Intent(Intent.ACTION_GET_CONTENT);
			} else {
				intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
			}
			intent.setType("video/*");
			intent.addCategory(Intent.CATEGORY_OPENABLE);

			try {
				startActivityForResult(Intent.createChooser(intent, "请选择一个视频文件"), ConfigUtil.UPLOAD_REQUEST);
			} catch (android.content.ActivityNotFoundException ex) {
				Toast.makeText(context, "请安装文件管理器", Toast.LENGTH_SHORT).show();
			}
		}
	};
	
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			
			int progress = binder.getProgress();
			UploadInfo uploadInfo = DataSet.getUploadInfo(currentUploadId);
			int position = uploadInfos.indexOf(uploadInfo);
			
			if (progress > 0 && uploadInfo != null && position >= 0) {
				uploadInfos.remove(position);
				
				uploadInfo.setProgress(progress);
				uploadInfo.setProgressText(binder.getProgressText());
				DataSet.updateUploadInfo(uploadInfo);
				
				uploadInfos.add(position, uploadInfo);
				uploadAdapter.notifyDataSetChanged();
				uploadListView.invalidate();
			}
			
			super.handleMessage(msg);
		}
		
	};
	
	// 通过定时器和Handler来更新进度条
	TimerTask timerTask = new TimerTask() {
		@Override
		public void run() {
			
			if (binder == null || binder.isStop() ) {
				return;
			}
			
			if (currentUploadId == null) {
				currentUploadId = binder.getUploadId();
			}
			
			if (uploadInfos.isEmpty() || currentUploadId == null) {
				return;
			}
			
			handler.sendEmptyMessage(0);
		}
	};
	
	private class UploadReceiver extends BroadcastReceiver{
	
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!isBind) {
				binderService();
			}
			
			String uploadId = intent.getStringExtra("uploadId");
			if (uploadId != null) {
				currentUploadId = uploadId;
			}
			
			//若状态为上传中，重置当前上传view的标记位置
			int uploadStatus = intent.getIntExtra("status", ParamsUtil.INVALID);
			if (uploadStatus == Uploader.UPLOAD) {
				currentUploadId = null;
			}
			
			//若Uploader当前状态为已上传，自动上传处于等待中状态的视频
			if (uploadStatus == Uploader.FINISH) {
				currentUploadId = null;
				for(UploadInfo uploadInfo : uploadInfos){
					if (uploadInfo.getStatus() == Uploader.WAIT) {
						startUploadService(uploadInfo);
						currentUploadId = uploadInfo.getUploadId();
						break;
					}
				}
			}
			
			initUploadList();
			uploadAdapter.notifyDataSetChanged();
			uploadListView.invalidate();
			
			// 若出现异常，提示用户处理
			int errorCode = intent.getIntExtra("errorCode", ParamsUtil.INVALID);
			if (errorCode == ErrorCode.NETWORK_ERROR.Value()) {
				Toast.makeText(context, "网络异常，请检查", Toast.LENGTH_SHORT).show();
			} else if (errorCode == ErrorCode.PROCESS_FAIL.Value()) {
				Toast.makeText(context, "上传失败，请重试", Toast.LENGTH_SHORT).show();
			} else if (errorCode == ErrorCode.INVALID_REQUEST.Value()) {
				Toast.makeText(context, "上传失败，请检查账户信息", Toast.LENGTH_SHORT).show();
			}
			
		}
		
	}
	
	private void startUploadService(UploadInfo uploadInfo) {
		Intent service = new Intent(context, UploadService.class);
		VideoInfo videoInfo = uploadInfo.getVideoInfo();
		service.putExtra("title", videoInfo.getTitle());
		service.putExtra("tag", videoInfo.getTags());
		service.putExtra("desc", videoInfo.getTags());
		service.putExtra("filePath", videoInfo.getFilePath());
		service.putExtra("uploadId", uploadInfo.getUploadId());
		
		String videoId = videoInfo.getVideoId();
		if (videoId != null && !"".equals(videoId)) {
			service.putExtra("videoId", videoId);
		}

		activity.startService(service);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		
		if (requestCode == ConfigUtil.UPLOAD_REQUEST) {
			
			Intent intent = new Intent(context, InputInfoActivity.class);
			String filePath = null;
			int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
			Uri uri = data.getData();

			if (sdkVersion >= 19) {
				filePath = PictureUtils.getPath_above19(getActivity(), uri);
			} else {
				filePath = PictureUtils.getFilePath_below19(getActivity(), uri);
			}

//			String filePath = getRealPath(data.getData());
			
			if (filePath == null) {
				Toast.makeText(context, "文件有误，请重新选择", Toast.LENGTH_SHORT).show();
				return;
			}
			intent.putExtra("filePath", filePath);
			startActivity(intent);
		}
	}
	
	@Override
	public void onDestroy() {
		
		activity.unbindService(serviceConnection);
		isBind = false;
		timerTask.cancel();
		activity.unregisterReceiver(receiver);
		
		super.onDestroy();
	}
	
	@SuppressLint("NewApi") 
	private String getRealPath(Uri uri){
		String filePath = null;
		String uriString = uri.toString();

		if(uriString.startsWith("content://media")){
			filePath = getDataColumn(context, uri, null, null);
		} else if (uriString.startsWith("file")){
			filePath = uri.getPath();
		} else if (uriString.startsWith("content://com")){
				String docId = DocumentsContract.getDocumentId(uri);
	            String[] split = docId.split(":");  
	            Uri contentUri = null;  
	            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;  
	            String selection = "_id=?";  
	            String[] selectionArgs = new String[] {split[1]};  
	            filePath = getDataColumn(context, contentUri, selection, selectionArgs);
		}
		
		return filePath;
	}
	
	private String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {  
	    Cursor cursor = null;  
	    String[] column = {MediaColumns.DATA};  
	  
	    try {  
	        cursor = context.getContentResolver().query(uri, column, selection, selectionArgs, null);  
	        if (cursor != null && cursor.moveToFirst()) {  
	            final int index = cursor.getColumnIndexOrThrow(column[0]);  
	            return cursor.getString(index);  
	        }  
	    } catch (Exception e) {
			Log.e("getRealPath error ", "exception: " + e);
	    } finally {  
	        if (cursor != null)  
	            cursor.close();  
	    }  
	    return null;  
	} 
}
