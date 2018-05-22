package com.bokecc.sdk.mobile.demo.adapter;

import java.util.List;

import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.bokecc.sdk.mobile.demo.view.VideoListView;

/**
 * 
 * 显示视频ID列表的适配器
 * 
 * @author CC视频
 *
 */
public class VideoListViewAdapter extends BaseAdapter{
	
	protected List<Pair<String, Integer>> pairs;
	
	protected Context context;
	
	public VideoListViewAdapter(Context context, List<Pair<String, Integer>> pairs){
		this.pairs = pairs;
		this.context = context;
	}

	@Override
	public int getCount() {
		return pairs.size();
	}

	@Override
	public Object getItem(int position) {
		return pairs.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		Pair<String, Integer> pair = pairs.get(position);
		VideoListView videoListView = new VideoListView(context, pair.first, pair.second);
		videoListView.setTag(pair.first);
		
		return videoListView;
		
	}

}
