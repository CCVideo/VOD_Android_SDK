package com.bokecc.vod.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bokecc.projection.ProjectionDevice;
import com.bokecc.vod.R;

import org.fourthline.cling.model.meta.Device;

import java.util.List;

public class DeviceAdapter extends BaseAdapter {
    private List<ProjectionDevice> datas;
    private LayoutInflater layoutInflater;

    public DeviceAdapter(Context context, List<ProjectionDevice> datas) {
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
            convertView = layoutInflater.inflate(R.layout.item_device, null);
            holder = new ViewHolder();
            holder.tv_device_name = convertView.findViewById(R.id.tv_device_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ProjectionDevice projectionDevice = datas.get(position);
        if (projectionDevice != null) {
            Device device = projectionDevice.getDevice();
            String deviceName = device.getDetails().getFriendlyName();
            holder.tv_device_name.setText(deviceName);
        }
        return convertView;
    }

    class ViewHolder {
        TextView tv_device_name;
    }
}
