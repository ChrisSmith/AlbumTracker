package org.collegelabs.albumtracker;

import org.collegelabs.library.bitmaploader.BitmapCache;

public class Application extends android.app.Application {

	private BitmapCache mBitmapCache = new BitmapCache();

	public BitmapCache getBitmapCache(){ return mBitmapCache; }

	@Override
	public void onCreate(){
		super.onCreate();
	}
}
