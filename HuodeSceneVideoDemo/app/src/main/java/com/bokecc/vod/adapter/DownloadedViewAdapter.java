package com.bokecc.vod.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bokecc.vod.R;
import com.bokecc.vod.download.DownloadWrapper;
import com.bokecc.vod.utils.MultiUtils;

import java.util.List;


public class DownloadedViewAdapter extends BaseAdapter{

	private List<DownloadWrapper> downloadInfos;

	private Context context;

	public DownloadedViewAdapter(Context context, List<DownloadWrapper> downloadInfos){
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
			holder = new ViewHolder();
			convertView = View.inflate(context, R.layout.item_downloaded_video, null);
			holder.tv_filesize = convertView.findViewById(R.id.tv_filesize);
			holder.titleView = convertView.findViewById(R.id.downloaded_title);
			holder.iv_video_cover = convertView.findViewById(R.id.iv_video_cover);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.titleView.setText(wrapper.getDownloadInfo().getTitle());
		long end = wrapper.getDownloadInfo().getEnd();
		if (end>0){
			holder.tv_filesize.setVisibility(View.VISIBLE);
			holder.tv_filesize.setText(Formatter.formatFileSize(context, wrapper.getDownloadInfo().getEnd()));
		}else {
			holder.tv_filesize.setVisibility(View.INVISIBLE);
		}
		MultiUtils.showVideoCover(holder.iv_video_cover,wrapper.getDownloadInfo().getVideoCover());
		return convertView;
	}

	public class ViewHolder {
		public TextView titleView;
		public TextView tv_filesize;
		public ImageView iv_video_cover;
	}
}
