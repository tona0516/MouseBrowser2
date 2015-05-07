package com.tona.mousebrowser2;

import java.util.Stack;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
public class MainActivity extends FragmentActivity {
	public static CustomViewPager viewPager;;
	DynamicFragmentPagerAdapter adapter;
	int currentPosition = 0, previousPosition = 0;
	int count = 0;
	public static Stack<Bundle> stack;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		stack = new Stack<Bundle>();
		viewPager = (CustomViewPager) findViewById(R.id.pager);
		viewPager.setOffscreenPageLimit(5);
		adapter = new DynamicFragmentPagerAdapter(getSupportFragmentManager());
		// adapter.add("page" + (count++), new
		// CustomWebViewFragment("http://www.amazon.co.jp/"));
		adapter.add("page" + (count++), new CustomWebViewFragment(null));
		viewPager.setAdapter(adapter);
		viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				super.onPageSelected(position);
				previousPosition = currentPosition;
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
			adapter.add("page" + (count++), new CustomWebViewFragment(null));
			adapter.notifyDataSetChanged();
		} else if (id == R.id.remove) {
			if (adapter.getCount() != 1) {
				adapter.remove(currentPosition);
				adapter.notifyDataSetChanged();
			}
		}
		return super.onOptionsItemSelected(item);
	}
}
