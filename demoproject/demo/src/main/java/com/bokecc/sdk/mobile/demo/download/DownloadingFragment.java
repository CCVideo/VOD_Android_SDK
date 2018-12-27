package com.bokecc.sdk.mobile.demo.download;

import java.io.File;
import java.util.List;

import android.content.Context;
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
import com.bokecc.sdk.mobile.demo.adapter.DownloadViewAdapter;
import com.bokecc.sdk.mobile.demo.downloadutil.DownloadController;
import com.bokecc.sdk.mobile.demo.downloadutil.DownloadController.Observer;
import com.bokecc.sdk.mobile.demo.downloadutil.DownloaderWrapper;
import com.bokecc.sdk.mobile.demo.util.ConfigUtil;
import com.bokecc.sdk.mobile.demo.util.MediaUtil;

/**
 * 下载中标签页
 * 
 * @author CC视频
 *
 */
public class DownloadingFragment extends Fragment implements Observer {

	private Context context;
	private FragmentActivity activity;

	private ListView downloadingListView;
	private List<DownloaderWrapper> downloadingInfos = DownloadController.downloadingList;
	private DownloadViewAdapter downloadAdapter;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		activity = getActivity();
		context = activity.getApplicationContext();
		RelativeLayout view = new RelativeLayout(context);
		initView(view);
		initData();
		return view;
	}

	private void initView(RelativeLayout view ){
		view.setBackgroundColor(Color.WHITE);
		LayoutParams downloadingLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		downloadingListView = new ListView(context);
		downloadingListView.setPadding(10, 10, 10, 10);
		downloadingListView.setDivider(getResources().getDrawable(R.drawable.line));
		view.addView(downloadingListView, downloadingLayoutParams);
		
		downloadingListView.setOnItemClickListener(onItemClickListener);
		downloadingListView.setOnCreateContextMenuListener(onCreateContextMenuListener);
		
	}

	private void initData() {
		downloadAdapter = new DownloadViewAdapter(context, downloadingInfos);
		downloadingListView.setAdapter(downloadAdapter);
	}

	ContextMenu contextMenu;
	OnCreateContextMenuListener onCreateContextMenuListener = new OnCreateContextMenuListener() {
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
			contextMenu = menu;
			menu.setHeaderTitle("操作");
			menu.add(ConfigUtil.DOWNLOADING_MENU_GROUP_ID, 0, 0, "删除");
		}
	};

	OnItemClickListener onItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			DownloadController.parseItemClick(position);
			updateListView();
		}
	};
	
	
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getGroupId() != ConfigUtil.DOWNLOADING_MENU_GROUP_ID) {
			return false;
		}
		
		int selectedPosition = ((AdapterContextMenuInfo) item.getMenuInfo()).position;// 获取点击了第几行
		DownloaderWrapper wrapper = (DownloaderWrapper) downloadAdapter.getItem(selectedPosition);
		String title = wrapper.getDownloadInfo().getTitle();

		String suffix = MediaUtil.M4A_SUFFIX;

		if (wrapper.getDownloadInfo().getDownloadMode() == 1) {
			suffix = MediaUtil.MP4_SUFFIX;
		}

		File file = new File(Environment.getExternalStorageDirectory() + "/" + ConfigUtil.DOWNLOAD_DIR + title + suffix);
		if(file.exists()){
			file.delete();
		}

		DownloadController.deleteDownloadingInfo(selectedPosition);

		updateListView();

		if (getUserVisibleHint()) {
			return true;
		}
		
		return false;
	}

	private void updateListView() {
		downloadAdapter.notifyDataSetChanged();
		downloadingListView.invalidate();
	}

	int downloadingCount = 0;
	@Override
	public void update() {
		activity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				updateListView();
				
				//为防止出现删除提示框展示的时候，新的下载视频完成，导致删除错误的bug，故当有新的下载完成时，取消删除对话框
				int currentDownloadingCount = DownloadController.downloadingList.size();
				if (currentDownloadingCount < downloadingCount) {
					downloadingCount = currentDownloadingCount;
					if (contextMenu != null) {
						contextMenu.close();
					}
				}
			}
		});
	}
	
	@Override
	public void onPause() {
		DownloadController.detach(this);
		super.onPause();
	}

	@Override
	public void onResume() {
		downloadingCount = DownloadController.downloadingList.size();
		DownloadController.attach(this);
		super.onResume();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}