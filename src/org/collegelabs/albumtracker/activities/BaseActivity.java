package org.collegelabs.albumtracker.activities;

import org.collegelabs.albumtracker.Constants;
import org.collegelabs.albumtracker.syncadapter.Settings;
import org.collegelabs.library.bitmaploader.caches.SimpleLruDiskCache;
import org.collegelabs.library.bitmaploader.caches.StrongBitmapCache;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class BaseActivity extends SherlockFragmentActivity {

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
	public StrongBitmapCache getBitmapCache(){
		return ((org.collegelabs.albumtracker.Application) getApplication()).getBitmapCache();
	}
	
	private SimpleLruDiskCache mCachePolicy;
	public SimpleLruDiskCache getBitmapCachePolicy(){
		return mCachePolicy;
	}
	
	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		mCachePolicy = new SimpleLruDiskCache(this);
		mCachePolicy.setHighWaterMark(10);
		mCachePolicy.setLowWaterMark(5);
	}
	
	public void onDestroy(){
		super.onDestroy();
		mCachePolicy.disconnect();
	}
}
