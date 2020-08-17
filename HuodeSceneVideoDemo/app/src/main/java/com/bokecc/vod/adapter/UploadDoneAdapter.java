package com.bokecc.vod.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bokecc.vod.R;
import com.bokecc.vod.download.DownloadWrapper;
import com.bokecc.vod.upload.UploadWrapper;
import com.bokecc.vod.utils.MultiUtils;

import java.io.File;
import java.util.List;


public class UploadDoneAdapter extends BaseAdapter{

	private List<UploadWrapper> uploadInfos;

	private Context context;

	public UploadDoneAdapter(Context context, List<UploadWrapper> uploadInfos){
		this.context = context;
		this.uploadInfos = uploadInfos;
	}

	@Override
	public int getCount() {
		return uploadInfos.size();
	}

	@Override
	public Object getItem(int position) {
		return uploadInfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		UploadWrapper wrapper = uploadInfos.get(position);
		ViewHolder holder = null;

		if (convertView == null) {
			holder = new ViewHolder();
			convertView = View.inflate(context, R.layout.item_upload_done, null);
			holder.tv_filesize = convertView.findViewById(R.id.tv_filesize);
			holder.tv_upload_title = convertView.findViewById(R.id.tv_upload_title);
			holder.iv_video_cover = convertView.findViewById(R.id.iv_video_cover);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.tv_upload_title.setText(wrapper.getUploadInfo().getTitle());
		holder.tv_filesize.setText(Formatter.formatFileSize(context, wrapper.getUploadInfo().getEnd()));
		String videoCoverPath = wrapper.getUploadInfo().getVideoCoverPath();
		if (!TextUtils.isEmpty(videoCoverPath)){
			MultiUtils.showCornerVideoCover(holder.iv_video_cover,videoCoverPath);
		}else {
			holder.iv_video_cover.setImageResource(R.mipmap.iv_default_img);
		}
		return convertView;
	}

	public class ViewHolder {
		public TextView tv_upload_title;
		public TextView tv_filesize;
		public ImageView iv_video_cover;
	}
}
