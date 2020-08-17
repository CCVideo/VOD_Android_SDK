package com.bokecc.vod.view;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * 字幕处理类
 * 
 * @author 获得场景视频
 *
 */
public class Subtitle {

	private final String REG = "\\d+\\r\\n(\\d{2}:\\d{2}:\\d{2},\\d{3}) --> (\\d{2}:\\d{2}:\\d{2},\\d{3})\\r\\n(.*?)\\r\\n\\r\\n";

	private int start;
	private int end;
	private String content;

	private List<Subtitle> subtitles;

	/**
	 * 字幕初始化监听器
	 *
	 */
	public interface OnSubtitleInitedListener {

		public void onInited(Subtitle subtitle);
	}

	private OnSubtitleInitedListener onSubtitleInitedListener;

	private Subtitle() {
	}

	public Subtitle(OnSubtitleInitedListener onSubtitleInitedListener) {
		this.onSubtitleInitedListener = onSubtitleInitedListener;
		this.subtitles = new ArrayList<Subtitle>();
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setResource() {

	}

	public void initSubtitleResource(final String subtitleUrl) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				HttpURLConnection connection = null;
				BufferedReader reader = null;

				try {
					URL url = new URL(subtitleUrl);
					connection = (HttpURLConnection) url.openConnection();
					//设置请求方法
					connection.setRequestMethod("GET");
					//设置连接超时时间（毫秒）
					connection.setConnectTimeout(5000);
					//设置读取超时时间（毫秒）
					connection.setReadTimeout(5000);
					//返回输入流
					InputStream in = connection.getInputStream();
					//读取输入流
					reader = new BufferedReader(new InputStreamReader(in));
					StringBuilder sb = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null) {
						sb.append(line+ "\r\n");
					}
					String result = sb.toString();
					parseSubtitleStr(result);
				} catch (Exception e) {
					Log.e("CCVideoViewDemo", "" + e.getMessage());
				}finally {
					if (reader != null) {
						try {
							reader.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					if (connection != null) {//关闭连接
						connection.disconnect();
					}
				}
			}
		}).start();
	}

	public String getSubtitleByTime(long time) {
		for (Subtitle subtitle : subtitles) {
			if (subtitle.getStart() <= time && time <= subtitle.getEnd()) {
				return subtitle.getContent();
			}
		}
		return "";
	}

	private void parseSubtitleStr(String results) {
		Pattern pattern = Pattern.compile(REG,Pattern.DOTALL);
		Matcher matcher = pattern.matcher(results);
		while (matcher.find()) {
			Subtitle subtitle = new Subtitle();
			subtitle.setStart(parseTime(matcher.group(1)));
			subtitle.setEnd(parseTime(matcher.group(2)));
			subtitle.setContent(matcher.group(3));
			subtitles.add(subtitle);
		}

		onSubtitleInitedListener.onInited(this);
	}

	private int parseTime(String timeStr) {
		int nReturn = 0;
		String[] times = timeStr.split(",");
		int nMs = Integer.parseInt(times[1]);
		String[] time = times[0].split(":");
		int nH = Integer.parseInt(time[0]);
		int nM = Integer.parseInt(time[1]);
		int nS = Integer.parseInt(time[2]);
		nReturn += nS * 1000;
		nReturn += nM * 60 * 1000;
		nReturn += nH * 60 * 60 * 1000;
		nReturn += nMs;
		return nReturn;
	}
}
