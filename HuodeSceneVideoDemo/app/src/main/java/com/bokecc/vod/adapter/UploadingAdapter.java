package com.bokecc.vod.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bokecc.sdk.mobile.download.Downloader;
import com.bokecc.sdk.mobile.upload.Uploader;
import com.bokecc.vod.R;
import com.bokecc.vod.upload.UploadWrapper;
import com.bokecc.vod.utils.MultiUtils;

import java.io.File;
import java.util.List;

public class UploadingAdapter extends BaseAdapter{

	private List<UploadWrapper> uploadInfos;

	private Context context;

	public UploadingAdapter(Context context, List<UploadWrapper> uploadInfos){
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
			convertView = View.inflate(context, R.layout.item_uploading, null);
			holder.tv_upload_title = convertView.findViewById(R.id.tv_upload_title);
			holder.tv_upload_status = convertView.findViewById(R.id.tv_upload_status);
			holder.tv_upload_speed =  convertView.findViewById(R.id.tv_upload_speed);
			holder.iv_video_cover = convertView.findViewById(R.id.iv_video_cover);
			holder.tv_upload_progress = convertView.findViewById(R.id.tv_upload_progress);
			holder.pb_upload = convertView.findViewById(R.id.pb_upload);
			holder.pb_upload.setMax(100);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.tv_upload_title.setText(wrapper.getUploadInfo().getTitle());
		holder.tv_upload_status.setText(getStatusStr(wrapper.getStatus()) + "");
		String videoCoverPath = wrapper.getUploadInfo().getVideoCoverPath();
		if (!TextUtils.isEmpty(videoCoverPath)){
			MultiUtils.showCornerVideoCover(holder.iv_video_cover,videoCoverPath);
		}else {
			holder.iv_video_cover.setImageResource(R.mipmap.iv_default_img);
		}

		if (wrapper.getStatus() == Downloader.DOWNLOAD) {
			holder.tv_upload_speed.setText(wrapper.getSpeed(context));
			holder.tv_upload_progress.setText(wrapper.getDownloadProgressText(context));
			holder.pb_upload.setProgress((int)wrapper.getDownloadProgressBarValue());
		} else {
			holder.tv_upload_speed.setText("");
			holder.tv_upload_progress.setText("");
			holder.pb_upload.setProgress((int)wrapper.getDownloadProgressBarValue());
		}

		return convertView;
	}

	private String getStatusStr(int status) {
		String statusStr = null;
		switch (status) {
			case Uploader.WAIT:
				statusStr = "等待中";
				break;
			case Uploader.UPLOAD:
				statusStr = "上传中";
				break;
			case Uploader.PAUSE:
				statusStr = "已暂停";
				break;
			case Uploader.FINISH:
				statusStr = "已完成";
				break;
		}

		return statusStr;
	}

	public class ViewHolder {
		public TextView tv_upload_title;
		public ImageView iv_video_cover;
		public TextView tv_upload_status;
		public TextView tv_upload_speed;
		public TextView tv_upload_progress;
		public ProgressBar pb_upload;
	}
}
