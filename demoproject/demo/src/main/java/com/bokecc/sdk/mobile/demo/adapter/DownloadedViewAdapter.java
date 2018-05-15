package com.bokecc.sdk.mobile.demo.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bokecc.sdk.mobile.demo.R;
import com.bokecc.sdk.mobile.demo.downloadutil.DownloaderWrapper;
import com.bokecc.sdk.mobile.download.Downloader;

import java.util.List;

public class DownloadedViewAdapter extends BaseAdapter{

	private List<DownloaderWrapper> downloadInfos;

	private Context context;

	public DownloadedViewAdapter(Context context, List<DownloaderWrapper> downloadInfos){
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
			RelativeLayout layout = (RelativeLayout) View.inflate(context, R.layout.downloaded_single_layout, null);
			convertView = layout;
			TextView titleView = (TextView) layout.findViewById(R.id.downloaded_title);

			holder = new ViewHolder();
			holder.titleView = titleView;

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.titleView.setText(wrapper.getDownloadInfo().getTitle());

		return convertView;
	}

	public class ViewHolder {
		public TextView titleView;
	}
}
