package org.collegelabs.albumtracker.syncadapter;

import org.collegelabs.albumtracker.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class Settings extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.activity_settings);
		
	}
	
	
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference){
		final String title = preference.getTitle().toString();
		if(title.equals("About")){
			showAboutDialog();
		}
		
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
	
	
	private void showAboutDialog(){
		
		Dialog d = new AlertDialog.Builder(this)
		.setMessage(R.string.about)
        .setTitle("About")
		.setPositiveButton(android.R.string.ok, null)
		.show();
		
		((TextView) d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
	}
}
