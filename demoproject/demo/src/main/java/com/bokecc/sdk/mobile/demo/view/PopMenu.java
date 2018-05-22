package com.bokecc.sdk.mobile.demo.view;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.bokecc.sdk.mobile.demo.R;
import com.bokecc.sdk.mobile.demo.util.ParamsUtil;

/**
 * 
 * 弹出菜单
 * 
 * @author CC视频
 *
 */
public class PopMenu implements OnItemClickListener {
	
	public interface OnItemClickListener {
		public void onItemClick(int position);
	}

	private ArrayList<String> itemList;
	private Context context;
	private PopupWindow popupWindow;
	private ListView listView;
	private OnItemClickListener listener;
	private int checkedPosition;

	public PopMenu(Context context, int resid, int checkedPosition, int height) {
		this.context = context;
		this.checkedPosition = checkedPosition;

		itemList = new ArrayList<String>();
		RelativeLayout view = new RelativeLayout(context);
//		view.setBackgroundResource(resid);

		listView = new ListView(context);
		listView.setPadding(0, ParamsUtil.dpToPx(context, 3), 0, ParamsUtil.dpToPx(context, 3));
		view.addView(listView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		listView.setAdapter(new PopAdapter());
		listView.setOnItemClickListener(this);

		popupWindow = new PopupWindow(view, context.getResources().getDimensionPixelSize(R.dimen.popmenu_width), height);
		popupWindow.setBackgroundDrawable(new ColorDrawable(Color.argb(178, 0, 0, 0)));
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (listener != null) {
			listener.onItemClick(position);
			checkedPosition = position;
			listView.invalidate();
		}
		
		dismiss();
	}

	public void setCheckedPosition(int position) {
		checkedPosition = position;
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.listener = listener;
	}

	public void addItems(String[] items) {
		for (String s : items)
			itemList.add(s);
	}
	
	public void addItem(String item) {
		itemList.add(item);
	}

	public void showAsDropDown(View parent) {
		popupWindow.showAsDropDown(parent, parent.getWidth() / 2 * -1, context.getResources().getDimensionPixelSize(R.dimen.popmenu_yoff));
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.update();
	}

	public void dismiss() {
		popupWindow.dismiss();
	}

	private final class PopAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return itemList.size();
		}

		@Override
		public Object getItem(int position) {
			return itemList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			RelativeLayout layoutView = new RelativeLayout(context);
			TextView textView = new TextView(context);
			textView.setTextSize(13);
			textView.setText(itemList.get(position));
			
			textView.setTag(position);
			
			if (checkedPosition == position || itemList.size() == 1) {
//				layoutView.setBackgroundColor(0x8033B5E5);
				textView.setTextColor(context.getResources().getColor(R.color.rb_text_check));
			} else {
				textView.setTextColor(Color.WHITE);
			}
			
			LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.CENTER_IN_PARENT);
			layoutView.addView(textView, params);
			layoutView.setMinimumHeight(ParamsUtil.dpToPx(context, 26));
			return layoutView;

		}

	}
}