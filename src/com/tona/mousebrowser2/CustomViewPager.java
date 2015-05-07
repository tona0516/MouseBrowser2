package com.tona.mousebrowser2;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CustomViewPager extends ViewPager {

	private boolean disable;

	public CustomViewPager(Context context) {
		super(context);
		setDisable(false);
	}

	public CustomViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (disable)
			return false;
		else
			return super.onInterceptTouchEvent(event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (disable)
			return false;
		else
			return super.onTouchEvent(event);
	}

	public void setDisable(boolean disable) {
		this.disable = disable;
	}
}
