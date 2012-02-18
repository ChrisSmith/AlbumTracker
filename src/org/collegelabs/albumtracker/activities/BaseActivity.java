package org.collegelabs.albumtracker.activities;

import org.collegelabs.albumtracker.Constants;
import org.collegelabs.albumtracker.syncadapter.Settings;
import org.collegelabs.library.bitmaploader.BitmapCache;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class BaseActivity extends FragmentActivity {

	@Override
	public void onStart(){
		super.onStart();
	}
	
	@Override 
	public void onStop(){
		super.onStop();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		menu.add(Menu.CATEGORY_SECONDARY, Constants.MENU_SETTINGS, Menu.NONE, "Settings");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case Constants.MENU_SETTINGS:
			startActivity(new Intent(this,Settings.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/*
	 * Return the bitmap cache that is managed by the Application (singleton)
	 */
	public BitmapCache getBitmapCache(){
		return ((org.collegelabs.albumtracker.Application) getApplication()).getBitmapCache();
	}
}
