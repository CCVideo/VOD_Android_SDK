package com.bokecc.sdk.mobile.demo.play;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import android.util.Log;

/**
 * 
 * 字幕处理类
 * 
 * @author CC视频
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

	public void initSubtitleResource(final String url) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					HttpClient client = new DefaultHttpClient();
					client.getParams().setParameter(
							CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
					HttpGet httpGet = new HttpGet(url);
					HttpResponse response = client.execute(httpGet);
					HttpEntity entity = response.getEntity();
					String results = EntityUtils.toString(entity, "utf-8");
					parseSubtitleStr(results);
				} catch (Exception e) {
					Log.e("CCVideoViewDemo", "" + e.getMessage());
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
		Pattern pattern = Pattern.compile(REG);
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
