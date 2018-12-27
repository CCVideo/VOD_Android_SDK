package com.bokecc.sdk.mobile.demo.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bokecc.sdk.mobile.demo.R;
import com.bokecc.sdk.mobile.demo.downloadutil.DownloaderWrapper;
import com.bokecc.sdk.mobile.download.Downloader;

public class DownloadViewAdapter extends BaseAdapter{
	
	private List<DownloaderWrapper> downloadInfos;

	private Context context;

	public DownloadViewAdapter(Context context, List<DownloaderWrapper> downloadInfos){
		this.context = context;
		this.downloadInfos = downloadInfos;
	}

	@Override
	public int getCount() {
		return downloadInfos.size();
	}

	@Override
	public Object getItem(int position) {
		return downloadInfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		DownloaderWrapper wrapper = downloadInfos.get(position);
		ViewHolder holder = null;

		if (convertView == null) {
			LinearLayout layout = (LinearLayout) View.inflate(context, R.layout.download_single_layout, null);
			convertView = layout;

			TextView titleView = (TextView) layout.findViewById(R.id.download_title);
			TextView statusView = (TextView) layout.findViewById(R.id.download_status);
			TextView speedView = (TextView) layout.findViewById(R.id.download_speed);
			TextView progressView = (TextView) layout.findViewById(R.id.download_progress);
			ProgressBar downloadProgressBar = (ProgressBar) layout.findViewById(R.id.download_progressBar);
			downloadProgressBar.setMax(100);

			holder = new ViewHolder();
			holder.downloadProgressBar = downloadProgressBar;
			holder.progressView = progressView;
			holder.speedView = speedView;
			holder.statusView = statusView;
			holder.titleView = titleView;

			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.titleView.setText(wrapper.getDownloadInfo().getTitle());
		holder.statusView.setText(getStatusStr(wrapper.getStatus()) + "");

		if (wrapper.getStatus() == Downloader.DOWNLOAD) {
			holder.speedView.setText(wrapper.getSpeed(context));
			holder.progressView.setText(wrapper.getDownloadProgressText(context));
			holder.downloadProgressBar.setProgress((int)wrapper.getDownloadProgressBarValue());
		} else {
			holder.speedView.setText("");
			holder.progressView.setText(wrapper.getDownloadProgressText(context));
			holder.downloadProgressBar.setProgress((int)wrapper.getDownloadProgressBarValue());
		}

		return convertView;
	}

	private String getStatusStr(int status) {
		String statusStr = null;
		switch (status) {
			case Downloader.WAIT:
				statusStr = "等待中";
				break;
			case Downloader.DOWNLOAD:
				statusStr = "下载中";
				break;
			case Downloader.PAUSE:
				statusStr = "已暂停";
				break;
			case Downloader.FINISH:
				statusStr = "已完成";
				break;
		}

		return statusStr;
	}

	public class ViewHolder {
		public TextView titleView;
		public TextView statusView;
		public TextView speedView;
		public TextView progressView;
		public ProgressBar downloadProgressBar;
	}
}
