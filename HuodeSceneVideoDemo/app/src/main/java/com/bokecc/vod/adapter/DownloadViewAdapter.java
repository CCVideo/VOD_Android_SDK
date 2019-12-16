package com.bokecc.vod.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bokecc.sdk.mobile.download.Downloader;
import com.bokecc.vod.R;
import com.bokecc.vod.download.DownloadWrapper;
import com.bokecc.vod.utils.MultiUtils;

import java.util.List;

public class DownloadViewAdapter extends BaseAdapter{
	
	private List<DownloadWrapper> downloadInfos;

	private Context context;

	public DownloadViewAdapter(Context context, List<DownloadWrapper> downloadInfos){
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
		DownloadWrapper wrapper = downloadInfos.get(position);
		ViewHolder holder = null;

		if (convertView == null) {
			convertView = View.inflate(context, R.layout.item_downloading, null);
			TextView titleView = convertView.findViewById(R.id.download_title);
			TextView statusView = convertView.findViewById(R.id.download_status);
			TextView speedView =  convertView.findViewById(R.id.download_speed);
			ImageView iv_video_cover = convertView.findViewById(R.id.iv_video_cover);
			TextView progressView = convertView.findViewById(R.id.download_progress);
			ProgressBar downloadProgressBar = convertView.findViewById(R.id.download_progressBar);
			downloadProgressBar.setMax(100);

			holder = new ViewHolder();
			holder.downloadProgressBar = downloadProgressBar;
			holder.progressView = progressView;
			holder.speedView = speedView;
			holder.statusView = statusView;
			holder.titleView = titleView;
			holder.iv_video_cover = iv_video_cover;
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.titleView.setText(wrapper.getDownloadInfo().getTitle());
		holder.statusView.setText(getStatusStr(wrapper.getStatus()) + "");
		MultiUtils.showVideoCover(holder.iv_video_cover,wrapper.getDownloadInfo().getVideoCover());

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
		public ImageView iv_video_cover;
		public TextView statusView;
		public TextView speedView;
		public TextView progressView;
		public ProgressBar downloadProgressBar;
	}
}
