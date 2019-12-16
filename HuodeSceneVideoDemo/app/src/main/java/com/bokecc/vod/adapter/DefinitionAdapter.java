package com.bokecc.vod.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bokecc.vod.R;
import com.bokecc.vod.data.DefinitionInfo;

import java.util.List;

public class DefinitionAdapter extends BaseAdapter {
    private List<DefinitionInfo> datas;
    private LayoutInflater layoutInflater;
    private Context context;

    public DefinitionAdapter(Context context, List<DefinitionInfo> datas) {
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
            convertView = layoutInflater.inflate(R.layout.item_definition, null);
            holder = new ViewHolder();
            holder.tv_definition = (TextView) convertView.findViewById(R.id.tv_definition);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        DefinitionInfo definitionInfo = datas.get(position);
        if (definitionInfo != null) {
            holder.tv_definition.setText(definitionInfo.getDefinitionText());
            if (definitionInfo.isSelected()){
                holder.tv_definition.setTextColor(context.getResources().getColor(R.color.orange));
            }else {
                holder.tv_definition.setTextColor(context.getResources().getColor(R.color.white));
            }
        }
        return convertView;
    }

    class ViewHolder {
        TextView tv_definition;
    }
}
