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

public class SelectVideoAdapter extends BaseAdapter {
    private List<HuodeVideoInfo> datas;
    private LayoutInflater layoutInflater;
    private Context context;

    public SelectVideoAdapter(Context context, List<HuodeVideoInfo> datas) {
        this.datas = datas;
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
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
            convertView = layoutInflater.inflate(R.layout.item_select_video, null);
            holder = new ViewHolder();
            holder.iv_video_img = (ImageView) convertView.findViewById(R.id.iv_video_img);
            holder.tv_video_title = (TextView) convertView.findViewById(R.id.tv_video_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        HuodeVideoInfo videoInfo = datas.get(position);
        if (videoInfo != null) {
            MultiUtils.showVideoCover(holder.iv_video_img,videoInfo.getVideoCover());
            holder.tv_video_title.setText(videoInfo.getVideoTitle());
            if (videoInfo.isSelected()){
                holder.tv_video_title.setTextColor(context.getResources().getColor(R.color.orange));
            }else {
                holder.tv_video_title.setTextColor(context.getResources().getColor(R.color.white));
            }
        }
        return convertView;
    }

    class ViewHolder {
        ImageView iv_video_img;
        TextView tv_video_title;
    }
}
