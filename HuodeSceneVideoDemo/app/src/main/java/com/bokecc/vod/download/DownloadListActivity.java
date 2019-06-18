package com.bokecc.vod.download;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bokecc.vod.R;
import com.bokecc.vod.ConfigUtil;
import com.bokecc.vod.utils.MultiUtils;


/**
 * 下载列表界面
 */
public class DownloadListActivity extends FragmentActivity {

	private ViewPager viewPager;
	public static String[] TAB_TITLE = {"下载中","已下载"};
	private TabFragmentPagerAdapter adapter;
	private ImageView iv_back;
	private TextView tv_downloading,tv_downloaded;
	private View line_downloading,line_downloaded;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download_list);
		MultiUtils.setStatusBarColor(this,R.color.transparent,true);

		initView();
		iv_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		tv_downloading.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectDownloading();
				viewPager.setCurrentItem(0);
			}
		});

		tv_downloaded.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectDownloaded();
				viewPager.setCurrentItem(1);
			}
		});
	}

	private void selectDownloaded() {
		tv_downloading.setTextColor(getResources().getColor(R.color.gray));
		tv_downloaded.setTextColor(getResources().getColor(R.color.orange));
		line_downloading.setVisibility(View.INVISIBLE);
		line_downloaded.setVisibility(View.VISIBLE);
	}

	private void selectDownloading() {
		tv_downloading.setTextColor(getResources().getColor(R.color.orange));
		tv_downloaded.setTextColor(getResources().getColor(R.color.gray));
		line_downloading.setVisibility(View.VISIBLE);
		line_downloaded.setVisibility(View.INVISIBLE);
	}

	private void initView() {
		viewPager = findViewById(R.id.downloadListPage);
		iv_back = findViewById(R.id.iv_back);
		tv_downloading = findViewById(R.id.tv_downloading);
		tv_downloaded = findViewById(R.id.tv_downloaded);
		line_downloading = findViewById(R.id.line_downloading);
		line_downloaded = findViewById(R.id.line_downloaded);

		adapter = new TabFragmentPagerAdapter(getSupportFragmentManager());
		viewPager.setAdapter(adapter);
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrolled(int i, float v, int i1) {

			}

			@Override
			public void onPageSelected(int i) {
				if (i==0){
					selectDownloading();
				}else if (i==1){
					selectDownloaded();
				}
			}

			@Override
			public void onPageScrollStateChanged(int i) {

			}
		});
	}

	public static class TabFragmentPagerAdapter extends FragmentPagerAdapter {

		private Fragment[] fragments;
		
		public TabFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
			fragments = new Fragment[]{new DownloadingFragment(),new DownloadedFragment()};
		}

		@Override
		public Fragment getItem(int arg0) {
			return fragments[arg0];
		}

		@Override
		public int getCount() {

			return ConfigUtil.DOWNLOAD_FRAGMENT_MAX_TAB_SIZE;
		}

		@Override
		public CharSequence getPageTitle(int index) {
			return TAB_TITLE[index];
		}
	}

	@Override

	protected void onResume() {
		super.onResume();
		//启动后台下载service
		Intent intent = new Intent(this, DownloadService.class);
		startService(intent);
	}

}
