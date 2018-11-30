package com.bokecc.sdk.mobile.demo.download;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.bokecc.sdk.mobile.demo.R;
import com.bokecc.sdk.mobile.demo.adapter.DownloadedViewAdapter;
import com.bokecc.sdk.mobile.demo.downloadutil.DownloadController;
import com.bokecc.sdk.mobile.demo.downloadutil.DownloadController.Observer;
import com.bokecc.sdk.mobile.demo.downloadutil.DownloaderWrapper;
import com.bokecc.sdk.mobile.demo.play.MediaPlayActivity;
import com.bokecc.sdk.mobile.demo.play.SpeedIjkMediaPlayActivity;
import com.bokecc.sdk.mobile.demo.util.ConfigUtil;
import com.bokecc.sdk.mobile.demo.util.MediaUtil;

/**
 * 已下载标签页
 * 
 * @author CC视频
 *
 */
public class DownloadedFragment extends Fragment implements Observer{

	private ListView downloadedListView;

	private List<DownloaderWrapper> downloadedInfos = DownloadController.downloadedList;
	
	private Context context;

	private DownloadedViewAdapter videoListViewAdapter;
	
	private FragmentActivity activity;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		activity = getActivity();
		context = activity.getApplicationContext();
		RelativeLayout downloadLayout = new RelativeLayout(activity.getApplicationContext());
		downloadLayout.setBackgroundColor(Color.WHITE);
		
		downloadedListView = new ListView(context);
		downloadedListView.setPadding(10, 10, 10, 10);
		downloadedListView.setDivider(getResources().getDrawable(R.drawable.line));
		LayoutParams downloadedLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		downloadLayout.addView(downloadedListView, downloadedLayoutParams);

		initData();

		downloadedListView.setOnItemClickListener(onItemClickListener);
		downloadedListView.setOnCreateContextMenuListener(onCreateContextMenuListener);

		return downloadLayout;
	}
	
	private void initData(){
		videoListViewAdapter = new DownloadedViewAdapter(context, downloadedInfos);
		downloadedListView.setAdapter(videoListViewAdapter);
	}

	OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@SuppressWarnings("unchecked")
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			DownloaderWrapper wrapper = (DownloaderWrapper) parent.getItemAtPosition(position);
			Intent intent = new Intent(context, MediaPlayActivity.class);
//			Intent intent = new Intent(context, SpeedIjkMediaPlayActivity.class);
			intent.putExtra("videoId", wrapper.getDownloadInfo().getTitle());
			intent.putExtra("isLocalPlay", true);
			intent.putExtra("playMode", wrapper.getDownloadInfo().getDownloadMode());
			startActivity(intent);
		}
	};

	OnCreateContextMenuListener onCreateContextMenuListener = new OnCreateContextMenuListener() {
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
			menu.setHeaderTitle("操作");
			menu.add(ConfigUtil.DOWNLOADED_MENU_GROUP_ID, 0, 0, "删除");
		}
	};
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getGroupId() != ConfigUtil.DOWNLOADED_MENU_GROUP_ID) {
			return false;
		}
		
		int selectedPosition = ((AdapterContextMenuInfo) item.getMenuInfo()).position;

		DownloaderWrapper wrapper = (DownloaderWrapper)videoListViewAdapter.getItem(selectedPosition);
		DownloadController.deleteDownloadedInfo(selectedPosition);

		//获取文件的后缀
		String suffix = MediaUtil.M4A_SUFFIX;

		if (wrapper.getDownloadInfo().getDownloadMode() == 1) {
			suffix = MediaUtil.MP4_SUFFIX;
		}


		File file = new File(Environment.getExternalStorageDirectory()+"/"+ConfigUtil.DOWNLOAD_DIR, wrapper.getDownloadInfo().getTitle()+ suffix);
		if(file.exists()){
			file.delete();
		}
		
		updateView();
	
		if (getUserVisibleHint()) {
			return true;
		}

		return false;
	}

	@Override
	public void update() {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				updateView();
			}
		});
	}
	
	private void updateView() {
		videoListViewAdapter.notifyDataSetChanged();
		downloadedListView.invalidate();
	}
	
	@Override
	public void onPause() {
		DownloadController.detach(this);
		super.onPause();
	}

	@Override
	public void onResume() {
		DownloadController.attach(this);
		super.onResume();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
				
}