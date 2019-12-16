package com.bokecc.vod.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bokecc.vod.R;
import com.bokecc.vod.data.ExerciseAnswer;

import java.util.List;

public class ExerciseAnswerAdapter extends BaseAdapter {
    private List<ExerciseAnswer> datas;
    private LayoutInflater layoutInflater;
    private Context context;

    public ExerciseAnswerAdapter(Context context, List<ExerciseAnswer> datas) {
        this.datas = datas;
        this.context = context;
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
            convertView = layoutInflater.inflate(R.layout.item_exercise_answer, null);
            holder = new ViewHolder();
            holder.iv_answer_result = convertView.findViewById(R.id.iv_answer_result);
            holder.tv_answer_content = convertView.findViewById(R.id.tv_answer_content);
            holder.ll_answer = convertView.findViewById(R.id.ll_answer);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ExerciseAnswer answer = datas.get(position);
        if (answer != null) {
            holder.tv_answer_content.setText(answer.getContent());
            if (answer.isRight()){
                holder.iv_answer_result.setImageResource(R.mipmap.iv_right_answer);
            }else {
                holder.iv_answer_result.setImageResource(R.mipmap.iv_error_answer);
            }

            if (answer.isMultiSelect()){
                if (answer.isSelected()){
                    if (answer.isCommit()){
                        holder.tv_answer_content.setTextColor(context.getResources().getColor(R.color.white));
                        holder.iv_answer_result.setVisibility(View.VISIBLE);
                        if (answer.isRight()){
                            holder.ll_answer.setBackgroundResource(R.drawable.right_answer_corner_bac);
                        }else {
                            holder.ll_answer.setBackgroundResource(R.drawable.error_answer_corner_bac);
                        }
                    }else {
                        holder.tv_answer_content.setTextColor(context.getResources().getColor(R.color.white));
                        holder.ll_answer.setBackgroundResource(R.drawable.selected_answer_corner_bac);
                        holder.iv_answer_result.setVisibility(View.INVISIBLE);
                    }

                }else {
                    if (answer.isCommit()){
                        if (answer.isRight()){
                            holder.tv_answer_content.setTextColor(context.getResources().getColor(R.color.white));
                            holder.iv_answer_result.setVisibility(View.VISIBLE);
                            holder.ll_answer.setBackgroundResource(R.drawable.right_answer_corner_bac);
                        }else {
                            holder.tv_answer_content.setTextColor(context.getResources().getColor(R.color.gray));
                            holder.ll_answer.setBackgroundResource(R.drawable.edit_videoinfo_corner_bac);
                            holder.iv_answer_result.setVisibility(View.INVISIBLE);
                        }
                    }else {
                        holder.tv_answer_content.setTextColor(context.getResources().getColor(R.color.exeTitle));
                        holder.ll_answer.setBackgroundResource(R.drawable.edit_videoinfo_corner_bac);
                        holder.iv_answer_result.setVisibility(View.INVISIBLE);
                    }

                }
            }else {
                if (answer.isSelected()){
                    holder.tv_answer_content.setTextColor(context.getResources().getColor(R.color.white));
                    holder.iv_answer_result.setVisibility(View.VISIBLE);
                    if (answer.isRight()){
                        holder.ll_answer.setBackgroundResource(R.drawable.right_answer_corner_bac);
                    }else {
                        holder.ll_answer.setBackgroundResource(R.drawable.error_answer_corner_bac);
                    }
                }else {
                    if (answer.isCommit()){
                        holder.tv_answer_content.setTextColor(context.getResources().getColor(R.color.gray));
                    }else {
                        holder.tv_answer_content.setTextColor(context.getResources().getColor(R.color.exeTitle));
                    }
                    holder.ll_answer.setBackgroundResource(R.drawable.edit_videoinfo_corner_bac);
                    holder.iv_answer_result.setVisibility(View.INVISIBLE);
                }
            }

        }


        return convertView;
    }

    class ViewHolder {
        TextView tv_answer_content;
        ImageView iv_answer_result;
        LinearLayout ll_answer;
    }
}
