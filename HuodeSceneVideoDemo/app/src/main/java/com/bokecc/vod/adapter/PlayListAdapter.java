package com.bokecc.vod.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bokecc.vod.R;
import com.bokecc.vod.data.HuodeVideoInfo;
import com.bokecc.vod.utils.MultiUtils;

import java.util.List;

public class PlayListAdapter extends BaseAdapter {
    private List<HuodeVideoInfo> datas;
    private LayoutInflater layoutInflater;
    private Context context;

    public PlayListAdapter(Context context, List<HuodeVideoInfo> datas) {
        this.context = context;
        this.datas = datas;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_play_list, null);
            holder = new ViewHolder();
            holder.iv_video_img = (ImageView) convertView.findViewById(R.id.iv_video_img);
            holder.iv_select_button = (ImageView) convertView.findViewById(R.id.iv_select_button);
            holder.tv_video_title = (TextView) convertView.findViewById(R.id.tv_video_title);
            holder.tv_video_time = (TextView) convertView.findViewById(R.id.tv_video_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        HuodeVideoInfo videoInfo = datas.get(position);
        if (videoInfo != null) {
            MultiUtils.showCornerVideoCover(holder.iv_video_img,videoInfo.getVideoCover());
            holder.tv_video_title.setText(videoInfo.getVideoTitle());
            holder.tv_video_time.setText(videoInfo.getVideoTime());
            if (videoInfo.isShowSelectButton()){
                holder.iv_select_button.setVisibility(View.VISIBLE);
            }else {
                holder.iv_select_button.setVisibility(View.GONE);
            }
            if (videoInfo.isSelectedDownload()){
                holder.iv_select_button.setImageResource(R.mipmap.iv_selected);
            }else {
                holder.iv_select_button.setImageResource(R.mipmap.iv_unselected);
            }

            if (videoInfo.isSelected()){
                holder.tv_video_title.setTextColor(context.getResources().getColor(R.color.orange));
            }else {
                holder.tv_video_title.setTextColor(context.getResources().getColor(R.color.black));
            }
        }
        return convertView;
    }

    class ViewHolder {
        ImageView iv_video_img;
        ImageView iv_select_button;
        TextView tv_video_title;
        TextView tv_video_time;
    }
}
