package com.bokecc.vod.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bokecc.vod.R;
import com.bokecc.vod.data.DanmuColorInfo;

import java.util.List;


public class DanmuColorAdapter extends RecyclerView.Adapter<DanmuColorAdapter.ViewHolder> {

    private List<DanmuColorInfo> mData;
    private boolean isPortrait = false;
    private DanmuColorAdapter.OnItemClickListener onItemClickListener;

    public DanmuColorAdapter(List<DanmuColorInfo> mData, boolean isPortrait) {
        this.mData = mData;
        this.isPortrait = isPortrait;
    }

    public interface OnItemClickListener {
        void onItemClick(DanmuColorInfo item, int position);
    }


    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;
        if (isPortrait) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_portrait_danmu_color, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_danmu_color, parent, false);
        }
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        DanmuColorInfo danmuColorInfo = mData.get(position);
        if (danmuColorInfo.isSelected()) {
            holder.iv_danmu_color.setImageResource(danmuColorInfo.getSelectdImgRes());
        } else {
            holder.iv_danmu_color.setImageResource(danmuColorInfo.getNormalImgRes());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (onItemClickListener != null) {
                    int pos = holder.getLayoutPosition();
                    onItemClickListener.onItemClick(mData.get(pos), pos);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_danmu_color;

        public ViewHolder(View itemView) {
            super(itemView);
            iv_danmu_color = itemView.findViewById(R.id.iv_danmu_color);
        }
    }
}
