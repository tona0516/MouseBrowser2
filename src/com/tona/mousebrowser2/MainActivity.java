package com.tona.mousebrowser2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;
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
		super.onCreate(null); //これでUnable to instantiate Fragmentを回避
		Log.d("onCreate", savedInstanceState+"");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		main = this;
		lastPageList = readPreference();

		viewPager = (CustomViewPager) findViewById(R.id.pager);
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
		// Log.d("lastIndex", currentPosition + "");
		// viewPager.setCurrentItem(currentPosition);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.reload) {
			adapter.get(currentPosition).getWebView().reload();
		} else if (id == R.id.general_settings) {
			startActivity(new Intent(getApplicationContext(), GeneralPref.class));
			// Toast.makeText(getApplicationContext(), "未作成",
			// Toast.LENGTH_SHORT).show();
		} else if (id == R.id.cursor_settings) {
			startActivity(new Intent(getApplicationContext(), Pref.class));
		} else if (id == R.id.create) {
			createFragment(null);
		} else if (id == R.id.remove) {
			removeFragment();
		} else if (id == R.id.url_bar) {
			final EditText e = adapter.get(currentPosition).getEditForm();
			e.setVisibility(View.VISIBLE);
			e.requestFocus();
			e.setSelection(0, e.getText().length());
			//遅らせてフォーカスがセットされるのを待つ
			main.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Timer t = new Timer();
					t.schedule(new TimerTask() {
						@Override
						public void run() {
							InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							inputMethodManager.showSoftInput(e, InputMethodManager.SHOW_IMPLICIT);
						}
					}, 200);
				}
			});
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
