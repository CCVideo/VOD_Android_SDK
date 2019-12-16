package com.bokecc.vod.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bokecc.vod.R;
import com.bokecc.vod.data.ExerciseStatistic;

import java.util.List;

public class StatisticResultAdapter extends BaseAdapter {
    private List<ExerciseStatistic> datas;
    private LayoutInflater layoutInflater;
    private Context context;

    public StatisticResultAdapter(Context context, List<ExerciseStatistic> datas) {
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
            convertView = layoutInflater.inflate(R.layout.item_exercise_statistic, null);
            holder = new ViewHolder();
            holder.tv_question = convertView.findViewById(R.id.tv_question);
            holder.rl_statistic_right = convertView.findViewById(R.id.rl_statistic_right);
            holder.pb_statistic_right = convertView.findViewById(R.id.pb_statistic_right);
            holder.tv_right_rate = convertView.findViewById(R.id.tv_right_rate);
            holder.rl_statistic_error = convertView.findViewById(R.id.rl_statistic_error);
            holder.pb_statistic_error = convertView.findViewById(R.id.pb_statistic_error);
            holder.tv_error_rate = convertView.findViewById(R.id.tv_error_rate);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ExerciseStatistic exerciseStatistic = datas.get(position);
        if (exerciseStatistic != null) {
            holder.tv_question.setText("第" + (position+1) + "题");
            int accuracy = exerciseStatistic.getAccuracy();
            if (exerciseStatistic.isAnswerRight()) {
                holder.rl_statistic_right.setVisibility(View.VISIBLE);
                holder.rl_statistic_error.setVisibility(View.GONE);
                holder.pb_statistic_right.setProgress(accuracy);
                holder.tv_right_rate.setText(accuracy + "%");
            } else {
                holder.rl_statistic_right.setVisibility(View.GONE);
                holder.rl_statistic_error.setVisibility(View.VISIBLE);
                holder.pb_statistic_error.setProgress(accuracy);
                holder.tv_error_rate.setText((100 - accuracy) + "%");
            }
        }
        return convertView;
    }

    class ViewHolder {
        TextView tv_question;
        RelativeLayout rl_statistic_right;
        ProgressBar pb_statistic_right;
        TextView tv_right_rate;
        RelativeLayout rl_statistic_error;
        ProgressBar pb_statistic_error;
        TextView tv_error_rate;
    }
}
