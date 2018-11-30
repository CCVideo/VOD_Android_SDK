package com.bokecc.sdk.mobile.demo.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.bokecc.sdk.mobile.demo.R;

/**
 * 
 * 弹出菜单
 * 
 * @author CC视频
 *
 */
public class PlayTopPopupWindow{
	
	private Context context;
	private PopupWindow popupWindow;
	private RadioGroup rgSubtitle, rgScreenSize;
	private CheckNetwork checkNetwork;

	public PlayTopPopupWindow(Context context, int height) {
		this.context = context;
		View view = LayoutInflater.from(context).inflate(R.layout.play_top_menu, null);
		TextView tv_check_network = findById(R.id.tv_check_network, view);
		rgSubtitle = findById(R.id.rg_subtitle, view);
		rgScreenSize = findById(R.id.rg_screensize, view);
		
		popupWindow = new PopupWindow(view, height * 2 / 3, height);
		popupWindow.setBackgroundDrawable(new ColorDrawable(Color.argb(178, 0, 0, 0)));
		tv_check_network.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (checkNetwork!=null){
					checkNetwork.checkNet();
				}
			}
		});
	}

	public void setCheckNetwork(CheckNetwork checkNetwork) {
		this.checkNetwork = checkNetwork;
	}
	
	public void setSubtitleCheckLister(OnCheckedChangeListener listener) {
		rgSubtitle.setOnCheckedChangeListener(listener);
	}
	
	public void setScreenSizeCheckLister(OnCheckedChangeListener listener) {
		rgScreenSize.setOnCheckedChangeListener(listener);
	}


	public void showAsDropDown(View parent) {
		popupWindow.showAtLocation(parent, Gravity.RIGHT, 0, 0);
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.update();
	}

	public void dismiss() {
		popupWindow.dismiss();
	}
	
	@SuppressWarnings("unchecked")
	private <T extends View> T findById(int resId, View view) {
		return (T)view.findViewById(resId);
	}
}
