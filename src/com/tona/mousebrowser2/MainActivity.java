package com.tona.mousebrowser2;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.Toast;
public class MainActivity extends FragmentActivity {
	public static CustomViewPager viewPager;
	private DynamicFragmentPagerAdapter adapter;
	private int currentPosition = 0;
	private int previousPosition = 0;
	private int count = 0;
	private MainActivity main;

	private ArrayList<ArrayList<String>> urlList;
	private ArrayList<Integer> indexList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(null); // これでUnable to instantiate Fragmentを回避
		readHistoryList();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		main = this;

		viewPager = (CustomViewPager) findViewById(R.id.pager);
		viewPager.setOffscreenPageLimit(10);
		adapter = new DynamicFragmentPagerAdapter(getSupportFragmentManager());
		if (urlList.isEmpty()) {
			CustomWebViewFragment f = new CustomWebViewFragment(null);
			adapter.add("page" + (count++), f);
			addPagetoList(CustomWebViewFragment.HOME);
		} else {
			for (ArrayList<String> list : urlList) {
				CustomWebViewFragment f = new CustomWebViewFragment(list.get(list.size() - 1));
				adapter.add("page" + (count++), f);
			}
		}
		viewPager.setAdapter(adapter);
		viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				super.onPageSelected(position);
				previousPosition = currentPosition;
				currentPosition = position;
				Log.d("cur", currentPosition + "");
				Log.d("pre", previousPosition + "");
				WebView w = adapter.get(position).getWebView();
				if (w != null) {
					if (!w.isFocused()) {
						w.requestFocus();
					}
				}
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
		if (id == R.id.reload) {
			adapter.get(currentPosition).getWebView().reload();
		} else if (id == R.id.general_settings) {
			startActivity(new Intent(getApplicationContext(), GeneralPref.class));
		} else if (id == R.id.cursor_settings) {
			startActivity(new Intent(getApplicationContext(), Pref.class));
		} else if (id == R.id.create) {
			createFragment(CustomWebViewFragment.HOME);
		} else if (id == R.id.remove) {
			removeFragment();
		} else if (id == R.id.url_bar) {
			final EditText e = adapter.get(currentPosition).getEditForm();
			e.setVisibility(View.VISIBLE);
			e.requestFocus();
			e.setSelection(0, e.getText().length());
			// 遅らせてフォーカスがセットされるのを待つ
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
		CustomWebViewFragment f = new CustomWebViewFragment(url);
		adapter.add("page" + (count++), f);
		adapter.notifyDataSetChanged();
		addPagetoList(url);
		viewPager.setCurrentItem(adapter.getCount() - 1);
	}
	private void removeFragment() {
		if (adapter.getCount() != 1) {
			adapter.remove(currentPosition);
			adapter.notifyDataSetChanged();
			removePagetoList();
		}
	}
	@Override
	public void onBackPressed() {
		ArrayList<String> list = urlList.get(currentPosition);
		CustomWebViewFragment f = adapter.get(currentPosition);

		int i = indexList.get(currentPosition);
		if (i - 1 > -1) {
			f.getWebView().loadUrl(list.get(i - 1));
			indexList.set(currentPosition, i - 1);
			f.setIsReturn(true);
			return;
		} else {
			Toast.makeText(this, "last page", Toast.LENGTH_SHORT).show();
			return;
		}
	}

	private void addPagetoList(String url) {
		ArrayList<String> list = new ArrayList<String>();
		list.add(url);
		urlList.add(list);
		indexList.add(0);
		writeHistoryList();
	}

	public void setPagetoList(String url) {
		ArrayList<String> list = urlList.get(currentPosition);
		int i = indexList.get(currentPosition);
		if (i == list.size() - 1) { // 現在のページが最新なら
			list.add(url);
		} else { // そうでなければ
			list.set(i + 1, url);
		}
		indexList.set(currentPosition, i + 1);
		urlList.set(currentPosition, list);
		writeHistoryList();
	}

	private void removePagetoList() {
		urlList.remove(currentPosition);
		indexList.remove(currentPosition);
		writeHistoryList();
	}

	private void writeHistoryList() {
		Log.d("indexList", indexList + "");
		ObjectOutputStream oos = null;
		FileOutputStream fos = null;
		ObjectOutputStream oos2 = null;
		FileOutputStream fos2 = null;
		try {
			fos = openFileOutput("url.obj", MODE_PRIVATE);
			oos = new ObjectOutputStream(fos);
			fos2 = openFileOutput("index.obj", MODE_PRIVATE);
			oos2 = new ObjectOutputStream(fos2);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			oos.writeObject(urlList);
			oos2.writeObject(indexList);
			fos.flush();
			fos.close();
			oos.flush();
			oos.close();
			fos2.flush();
			fos2.close();
			oos2.flush();
			oos2.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readHistoryList() {
		ObjectInputStream ois = null;
		FileInputStream fis = null;
		ObjectInputStream ois2 = null;
		FileInputStream fis2 = null;
		try {
			fis = openFileInput("url.obj");
			ois = new ObjectInputStream(fis);
			fis2 = openFileInput("index.obj");
			ois2 = new ObjectInputStream(fis2);
		} catch (StreamCorruptedException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if (ois != null && ois2 != null) {
			try {
				urlList = (ArrayList<ArrayList<String>>) ois.readObject();
				indexList = (ArrayList<Integer>) ois2.readObject();
				fis.close();
				ois.close();
				fis2.close();
				ois2.close();
			} catch (OptionalDataException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (urlList == null)
			urlList = new ArrayList<ArrayList<String>>();
		if (indexList == null)
			indexList = new ArrayList<Integer>();
	}
}
