package org.collegelabs.albumtracker.syncadapter;

import org.collegelabs.albumtracker.R;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class Settings extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.activity_settings);
		
	}
	
	
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference){
		final String title = preference.getTitle().toString();
		
		if(title.equals("")){
		
		}
		
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
	
}
