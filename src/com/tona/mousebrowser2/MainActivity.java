package com.tona.mousebrowser2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Toast;
public class MainActivity extends FragmentActivity {
	public static CustomViewPager viewPager;
	private DynamicFragmentPagerAdapter adapter;
	private int currentPosition = 0;
	private int count = 0;
	private Editor editor;
	private ArrayList<String> lastPageList;
	private MainActivity main;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		main = this;
		lastPageList = readPreference();

		viewPager = (CustomViewPager) findViewById(R.id.pager);
		viewPager.setOffscreenPageLimit(5);
		adapter = new DynamicFragmentPagerAdapter(getSupportFragmentManager());
		if (lastPageList.isEmpty()) {
			Log.d("TAG", "empty");
			adapter.add("page" + (count++), new CustomWebViewFragment(null));
			addPagetoList(CustomWebViewFragment.HOME);
		} else {
			Log.d("TAG", "not empty");
			for (int i = 0; i < lastPageList.size(); i++) {
				String s = lastPageList.get(i);
				adapter.add("page" + (count++), new CustomWebViewFragment(s));
			}
		}
		viewPager.setAdapter(adapter);
		viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				super.onPageSelected(position);
				currentPosition = position;
				editor = main.getSharedPreferences("shared_preference", Context.MODE_PRIVATE).edit();
				editor.putInt("index", currentPosition);
				editor.commit();
				WebView w = adapter.get(position).getWebView();
				if (!w.isFocused()) {
					w.requestFocus();
				}
			}
		});
		Log.d("lastIndex", currentPosition + "");
		viewPager.setCurrentItem(currentPosition);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.general_settings) {
			// startActivity(new Intent(getApplicationContext(),
			// GeneralPref.class));
			Toast.makeText(getApplicationContext(), "未作成", Toast.LENGTH_SHORT).show();
		} else if (id == R.id.cursor_settings) {
			startActivity(new Intent(getApplicationContext(), Pref.class));
		} else if (id == R.id.create) {
			createFragment(null);
		} else if (id == R.id.remove) {
			removeFragment();
		}
		return super.onOptionsItemSelected(item);
	}

	public void createFragment(String url) {
		if (url == null) {
			adapter.add("page" + (count++), new CustomWebViewFragment(null));
			addPagetoList(CustomWebViewFragment.HOME);
		} else {
			adapter.add("page" + (count++), new CustomWebViewFragment(url));
			addPagetoList(url);
		}
		adapter.notifyDataSetChanged();
		viewPager.setCurrentItem(adapter.getCount() - 1);
	}

	private void removeFragment() {
		if (adapter.getCount() != 1) {
			adapter.remove(currentPosition);
			removePagetoList();
			adapter.notifyDataSetChanged();
		}
	}
	@Override
	public void onBackPressed() {
		WebView wv = adapter.get(currentPosition).getWebView();
		if (wv.canGoBack()) {
			wv.goBack();
			return;
		}
		super.onBackPressed();
	}

	private void addPagetoList(String url) {
		lastPageList.add(url);
		editor = main.getSharedPreferences("shared_preference", Context.MODE_PRIVATE).edit();
		editor.putString("list", lastPageList.toString());
		editor.commit();
		Log.d("add", lastPageList.toString());
	}

	public void setPagetoList(String url) {
		lastPageList.set(currentPosition, url);
		editor = main.getSharedPreferences("shared_preference", Context.MODE_PRIVATE).edit();
		editor.putString("list", lastPageList.toString());
		editor.commit();
		Log.d("set", lastPageList.toString());
	}

	private void removePagetoList() {
		lastPageList.remove(currentPosition);
		editor = main.getSharedPreferences("shared_preference", Context.MODE_PRIVATE).edit();
		editor.putString("list", lastPageList.toString());
		editor.commit();
		Log.d("remove", lastPageList.toString());
	}

	private ArrayList<String> readPreference() {
		ArrayList<String> list = new ArrayList<String>();

		// 一旦bundleに保存
		Bundle bundle = new Bundle(); // 保存用のバンドル
		Map<String, ?> prefKV = getApplicationContext().getSharedPreferences("shared_preference", Context.MODE_PRIVATE).getAll();
		Set<String> keys = prefKV.keySet();
		for (String key : keys) {
			Object value = prefKV.get(key);
			if (value instanceof String) {
				bundle.putString(key, (String) value);
			}
		}
		// listに書き込む
		String stringList = bundle.getString("list"); // key名が"list"のものを取り出す
		currentPosition = bundle.getInt("index");
		// 履歴がないときは新しいインスタンスを返す
		if (stringList == null)
			return list;
		stringList = stringList.replaceAll("\\[", "");
		stringList = stringList.replaceAll("\\]", "");

		if (stringList.contains(",")) {
			String[] splitter = stringList.split(",");
			list.addAll(Arrays.asList(splitter));
		} else {
			list.add(stringList);
		}
		Log.d("TAG", "" + list);
		return list;
	}
}
