package org.collegelabs.albumtracker;


import org.collegelabs.library.bitmaploader.caches.StrongBitmapCache;

import com.bugsense.trace.BugSenseHandler;

public class Application extends android.app.Application {

	private StrongBitmapCache mBitmapCache;

	public StrongBitmapCache getBitmapCache(){ return mBitmapCache; }
	
	@Override
	public void onCreate(){
		super.onCreate();

		mBitmapCache = StrongBitmapCache.build(this);
		
		if(!BuildConfig.DEBUG) 
			BugSenseHandler.setup(this, Utils.getApiKey(this, Utils.BUG_SENSE_KEY));	
	}
}
