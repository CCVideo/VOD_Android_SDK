package com.bokecc.sdk.mobile.demo.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.bokecc.sdk.mobile.demo.model.UploadInfo;
import com.bokecc.sdk.mobile.demo.view.UploadView;

/**
 * 显示上传列表的适配器
 * 
 * @author CC视频
 *
 */
public class UploadViewAdapter extends BaseAdapter {

	private Context context;

	private List<UploadInfo> uploadInfos;

	public UploadViewAdapter(Context context, List<UploadInfo> uploadInfos) {

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
		UploadInfo uploadInfo = uploadInfos.get(position);
		if (convertView == null) {
			UploadView uploadView = new UploadView(context, 
					uploadInfo.getUploadId(),
					uploadInfo.getBitmap(context),
					uploadInfo.getVideoInfo().getTitle(), 
					uploadInfo.getStatusInfo(),
					uploadInfo.getProgressText(),
					uploadInfo.getProgress());
			uploadView.setTag(uploadInfo.getUploadId());
			return uploadView;
			
		} else if (convertView instanceof UploadView) {
			
			UploadView uploadView = (UploadView) convertView;
			
			// 若当前视图为缓存视图，则只更新进度
			if (uploadView.getUploadId().equals(uploadInfo.getUploadId())) {
				
				uploadView.setProgress(uploadInfo.getProgress());
				uploadView.setProgressText(uploadInfo.getProgressText());
				
			} else {
				uploadView = new UploadView(context, 
						uploadInfo.getUploadId(),
						uploadInfo.getBitmap(context),
						uploadInfo.getVideoInfo().getTitle(), 
						uploadInfo.getStatusInfo(),
						uploadInfo.getProgressText(),
						uploadInfo.getProgress());
				uploadView.setTag(uploadInfo.getUploadId());
			}
			
			
			return uploadView;
			
		} else {
			
			return convertView;
		}
	}

}
