package org.collegelabs.albumtracker;

import org.collegelabs.library.bitmaploader.BitmapCache;

import com.bugsense.trace.BugSenseHandler;

public class Application extends android.app.Application {

	private BitmapCache mBitmapCache = new BitmapCache();

	public BitmapCache getBitmapCache(){ return mBitmapCache; }

	
	@Override
	public void onCreate(){
		super.onCreate();

		if(!Constants.DEBUG) 
			BugSenseHandler.setup(this, Utils.getApiKey(this, Utils.BUG_SENSE_KEY));	
	}
}
