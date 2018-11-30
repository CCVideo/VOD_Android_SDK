package com.bokecc.sdk.mobile.demo.adapter;

import java.util.List;

import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.bokecc.sdk.mobile.demo.util.ParamsUtil;

public class AccountViewAdapter extends BaseAdapter{

	private Context context;
	
	private List<Pair<String, String>> pairs;
	
	public AccountViewAdapter(Context context, List<Pair<String, String>> pairs){
		this.context = context;
		this.pairs = pairs;
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
		if (convertView == null) {
			
			return getItemView(pairs.get(position));
		} else {
			
			return convertView;
		}
	}
	
	private View getItemView(Pair<String, String> pair){
		RelativeLayout accountView = new RelativeLayout(context);
		TextView textView = new TextView(context);
		textView.setText(pair.first + " : " + pair.second);
		textView.setTextSize(16);
		textView.setPadding(10, 30, 0, 0);
		textView.setMinHeight(ParamsUtil.dpToPx(context, 48));
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_VERTICAL);
		
		accountView.addView(textView, params);
		return accountView;
		
	}

}
