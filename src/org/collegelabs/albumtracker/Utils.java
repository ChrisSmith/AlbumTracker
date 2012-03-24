package org.collegelabs.albumtracker;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.util.Log;

public class Utils {

	public static final String LAST_FM_KEY = "keys/lastfm.txt";
	public static final String BUG_SENSE_KEY = "keys/bugsense.txt";
	
	public static String getApiKey(final Context context, final String keyLocation){
		// If you want to use any of the available services for your fork, 
		// register with them and place your API key in /assets/keys/<service>.txt
		// This allows the project to be open source without exposing my api keys.
		String key = "";
		try {
			InputStream inputStream = context.getAssets().open(keyLocation);
			key = Utils.ReadInputStream(inputStream).trim();
			
		} catch (IOException e) {
			Log.e(Constants.TAG, "Unable to load api key ("+keyLocation+"). Please place your key in /assets/keys/<service>.txt");
		}	
		return key;
	}

	public static String ReadInputStream(InputStream in) throws IOException {
		StringBuffer stream = new StringBuffer();
		byte[] b = new byte[1024];
		for (int n; (n = in.read(b)) != -1;) {
			stream.append(new String(b, 0, n));
		}
		return stream.toString();
	}
}
