package org.collegelabs.albumtracker.syncadapter;

import org.collegelabs.albumtracker.BuildConfig;
import org.collegelabs.albumtracker.Constants;
import org.collegelabs.albumtracker.structures.Album;
import org.collegelabs.albumtracker.structures.ParseBuyLinksRunnable;
import org.collegelabs.library.bitmaploader.LoadNetworkBitmap;
import org.collegelabs.library.bitmaploader.caches.SimpleLruDiskCache;


import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class BackgroundService extends IntentService {

	public static final String ACTION_DOWNLOAD_IMAGE = "org.collegelabs.albumtracker.syncadapter.actions.download_image";
	public static final String ACTION_DOWNLOAD_BUYLINKS = "org.collegelabs.albumtracker.syncadapter.actions.download_buylinks";
	public static final String ACTION_CLEANUP_CACHE = "org.collegelabs.albumtracker.syncadapter.actions.cleanup_cache";
	
	public static final String EXTRA_URL = "extra_url";
	public static final String EXTRA_ALBUM = "album";

	public BackgroundService() {
		super("BackgroundService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getAction();
		if(action == null){
			if(BuildConfig.DEBUG) Log.e(Constants.TAG,"BackgroundService: no action was provided");
			return;
		}

		if(action.equals(ACTION_DOWNLOAD_IMAGE)){
			String url = intent.getStringExtra(EXTRA_URL);
			downloadImage(url);
		}else if(action.equals(ACTION_DOWNLOAD_BUYLINKS)){
			Album album = intent.getParcelableExtra(EXTRA_ALBUM);
			downloadBuylinks(album);
		}else if(action.equals(ACTION_CLEANUP_CACHE)){
			SimpleLruDiskCache cache = new SimpleLruDiskCache(this);
			cache.disconnect();
			cache.sweep();
		}else{
			if(BuildConfig.DEBUG) Log.e(Constants.TAG,"BackgroundService can't handle action: "+action);
		}
	}

	private void downloadBuylinks(Album album){ 
		new ParseBuyLinksRunnable(this, album).run();
	}

	private void downloadImage(String url){

		if(url==null){
			if(BuildConfig.DEBUG) Log.e(Constants.TAG,"BackgroundService can't download a null url");
			return;
		}
		
		if(BuildConfig.DEBUG) Log.d(Constants.TAG,"BackgroundService downloading: "+url);

		SimpleLruDiskCache cache = new SimpleLruDiskCache(this);
		new LoadNetworkBitmap(null, url, cache, null).run();
		cache.disconnect();
		
	}
}
