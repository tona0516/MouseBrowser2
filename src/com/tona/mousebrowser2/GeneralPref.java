package com.tona.mousebrowser2;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class GeneralPref extends PreferenceActivity{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.general_pref);
	}
}
