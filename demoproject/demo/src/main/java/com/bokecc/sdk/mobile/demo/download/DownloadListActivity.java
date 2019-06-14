package com.bokecc.sdk.mobile.demo.download;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.bokecc.sdk.mobile.demo.R;
import com.bokecc.sdk.mobile.demo.util.ConfigUtil;

/**
 * 下载列表界面
 * 
 * @author CC视频
 *
 */
public class DownloadListActivity extends FragmentActivity implements TabListener {

	private ViewPager viewPager;
	

	public static String[] TAB_TITLE = { "已下载", "下载中" };

	private TabFragmentPagerAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.download_list);

		viewPager = (ViewPager) this.findViewById(R.id.downloadListPage);

		initView();
	}

	private void initView() {

		final ActionBar actionBar = getActionBar();

		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		adapter = new TabFragmentPagerAdapter(getSupportFragmentManager());
		viewPager.setAdapter(adapter);
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {

				actionBar.setSelectedNavigationItem(arg0);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

		for (int i = 0; i < ConfigUtil.DOWNLOAD_FRAGMENT_MAX_TAB_SIZE; i++) {
			Tab tab = actionBar.newTab();
			tab.setText(adapter.getPageTitle(i)).setTabListener(this);
			actionBar.addTab(tab);
		}
	}

	public static class TabFragmentPagerAdapter extends FragmentPagerAdapter {

		private Fragment[] fragments;
		
		public TabFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
			fragments = new Fragment[]{new DownloadedFragment(), new DownloadingFragment()};
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
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.download_list, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {

	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
        //启动后台下载service
        Intent intent = new Intent(this, DownloadService.class);
        startService(intent);
	}



}
