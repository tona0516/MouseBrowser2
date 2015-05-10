package com.tona.mousebrowser2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebView;
public class MainActivity extends FragmentActivity {
	public static CustomViewPager viewPager;
	public static DynamicFragmentPagerAdapter adapter;
	private static int currentPosition = 0;
	public static int count = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		viewPager = (CustomViewPager) findViewById(R.id.pager);
		viewPager.setOffscreenPageLimit(5);
		adapter = new DynamicFragmentPagerAdapter(getSupportFragmentManager());
		adapter.add("page" + (count++), new CustomWebViewFragment(null));
		viewPager.setAdapter(adapter);
		viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				super.onPageSelected(position);
				currentPosition = position;
			}
		});
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.settings) {
			startActivity(new Intent(getApplicationContext(), Pref.class));
		} else if (id == R.id.create) {
			createFragment(null);
		} else if (id == R.id.remove) {
			removeFragment();
		}
		return super.onOptionsItemSelected(item);
	}

	public static void createFragment(String url) {
		if (url == null)
			adapter.add("page" + (count++), new CustomWebViewFragment(null));
		else
			adapter.add("page" + (count++), new CustomWebViewFragment(url));
		adapter.notifyDataSetChanged();
		viewPager.setCurrentItem(adapter.getCount() - 1);
	}

	public static void removeFragment() {
		if (adapter.getCount() != 1) {
			adapter.remove(currentPosition);
			adapter.notifyDataSetChanged();
		}
	}
	@Override
	public void onBackPressed() {
		WebView wv = adapter.get(currentPosition).getWebView();
		if(wv.canGoBack()){
			wv.goBack();
			return;
		}
		super.onBackPressed();
	}
}
