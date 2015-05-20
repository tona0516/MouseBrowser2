package com.tona.mousebrowser2;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
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
public class MainActivity extends FragmentActivity {
	public static CustomViewPager viewPager;
	private DynamicFragmentPagerAdapter adapter;
	private int currentPosition = 0;
	private int count = 0;
	private Editor editor;
	// private ArrayList<String> lastPageList;
	private MainActivity main;

	private CustomFragmentList webViewList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(null); // これでUnable to instantiate Fragmentを回避
		webViewList = readWebViewList();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		main = this;
		// lastPageList = readPreference();

		viewPager = (CustomViewPager) findViewById(R.id.pager);
		adapter = new DynamicFragmentPagerAdapter(getSupportFragmentManager());
		if (webViewList.isEmpty()) {
			CustomWebViewFragment f = new CustomWebViewFragment(null);
			adapter.add("page" + (count++), f);
			addPagetoList(f);
		} else {
			for (int i = 0; i < webViewList.size(); i++) {
				adapter.add("page" + (count++), webViewList.get(i));
			}
		}
		// if (lastPageList.isEmpty()) {
		// Log.d("TAG", "empty");
		// adapter.add("page" + (count++), new CustomWebViewFragment(null));
		// addPagetoList(CustomWebViewFragment.HOME);
		// } else {
		// Log.d("TAG", "not empty");
		// for (int i = 0; i < lastPageList.size(); i++) {
		// String s = lastPageList.get(i);
		// adapter.add("page" + (count++), new CustomWebViewFragment(s));
		// }
		// }
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
		addPagetoList(f);
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

	private void addPagetoList(CustomWebViewFragment v) {
		// lastPageList.add(url);
		// editor = main.getSharedPreferences("shared_preference",
		// Context.MODE_PRIVATE).edit();
		// editor.putString("list", lastPageList.toString());
		// editor.commit();
		// Log.d("add", lastPageList.toString());
		webViewList.add(v);
		writeWebViewList();
	}

	public void setPagetoList(CustomWebViewFragment v) {
		// lastPageList.set(currentPosition, url);
		// editor = main.getSharedPreferences("shared_preference",
		// Context.MODE_PRIVATE).edit();
		// editor.putString("list", lastPageList.toString());
		// editor.commit();
		// Log.d("set", lastPageList.toString());
		webViewList.set(currentPosition, v);
		writeWebViewList();
	}

	private void removePagetoList() {
		// lastPageList.remove(currentPosition);
		// editor = main.getSharedPreferences("shared_preference",
		// Context.MODE_PRIVATE).edit();
		// editor.putString("list", lastPageList.toString());
		// editor.commit();
		// Log.d("remove", lastPageList.toString());
		webViewList.remove(currentPosition);
		writeWebViewList();
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

	private void writeWebViewList() {
		ObjectOutputStream oos = null;
		FileOutputStream fos = null;
		try {
			fos = openFileOutput("webview", MODE_PRIVATE);
			oos = new ObjectOutputStream(fos);
		} catch (FileNotFoundException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}
		try {
			Log.d("TAG", fos + "");
			Log.d("TAG", oos + "");
			Log.d("write", webViewList + "");
			oos.writeObject(webViewList);
			fos.flush();
			fos.close();
			oos.flush();
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private CustomFragmentList readWebViewList() {
		Object o = null;
		ObjectInputStream ois = null;
		FileInputStream fis = null;
		try {
			fis = openFileInput("webview");
			ois = new ObjectInputStream(fis);
		} catch (StreamCorruptedException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}
		if (ois != null) {
			try {
				while ((o = ois.readObject()) != null) {
					if (o instanceof CustomFragmentList) {
						System.out.println(((CustomFragmentList) o).toString());
					}
				}
				fis.close();
				ois.close();
			} catch (OptionalDataException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Log.d("exist", o + "");
		if (o != null)
			return (CustomFragmentList) o;
		else
			return new CustomFragmentList();
	}

	public static class CustomFragmentList extends LinkedList<CustomWebViewFragment> implements Serializable {
		private static final long serialVersionUID = 19920516L;
	}
}
