package com.bokecc.sdk.mobile.demo.util;

import android.content.Context;

public class ParamsUtil {
	
	public final static int INVALID = -1;

	public static int getInt(String str){
		int num = INVALID;
		
		try {
			num = Integer.parseInt(str);
		} catch (NumberFormatException e) {
			
		}
		
		return num;
	}
	
	public static long getLong(String str){
		long num = 0l;
		
		try {
			num = Long.parseLong(str);
		} catch (NumberFormatException e) {
		}
		
		return num;
	}
	
	public static String byteToM(long num){
		double m = (double) num / 1024 / 1024;
		return String.format("%.2f", m);
	}
	
	public static String millsecondsToStr(int seconds){
		seconds = seconds / 1000;
		String result = "";
		int hour = 0, min = 0, second = 0;
		hour = seconds / 3600;
		min = (seconds - hour * 3600) / 60;
		second = seconds - hour * 3600 - min * 60;
		if (hour < 10) {
			result += "0" + hour + ":";
		} else {
			result += hour + ":";
		}
		if (min < 10) {
			result += "0" + min + ":";
		} else {
			result += min + ":";
		}
		if (second < 10) {
			result += "0" + second;
		} else {
			result += second;
		}
		return result;
	}

	public static String millsecondsToMinuteSecondStr(int seconds){
		seconds = seconds / 1000;
		String result = "";
		int min = 0, second = 0;
		min = seconds / 60;
		second = seconds - min * 60;

		if (min < 10) {
			result += "0" + min + ":";
		} else {
			result += min + ":";
		}
		if (second < 10) {
			result += "0" + second;
		} else {
			result += second;
		}
		return result;
	}

	/**
	 * convert dp to its equivalent px
	 *
	 * 将dp转换为与之相等的px
	 */
	public static int dpToPx(Context context, int dipValue){
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	/**
	 * convert sp to its equivalent px
	 *
	 * 将sp转换为px
	 */
	public static int sp2px(Context context, float spValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}
}
