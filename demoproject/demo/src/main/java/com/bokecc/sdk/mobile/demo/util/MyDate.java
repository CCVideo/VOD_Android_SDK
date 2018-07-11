package com.bokecc.sdk.mobile.demo.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyDate {
	public static String getFileName() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String date = format.format(new Date(System.currentTimeMillis()));
		return date;
	}

	public static String getDateEN() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date1 = format.format(new Date(System.currentTimeMillis()));
		return date1;
	}

}