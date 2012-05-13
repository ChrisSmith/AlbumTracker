package org.collegelabs.albumtracker.loaders;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.collegelabs.albumtracker.BuildConfig;
import org.collegelabs.albumtracker.Constants;
import org.collegelabs.albumtracker.LastfmHelper;
import org.collegelabs.albumtracker.Utils;
import org.collegelabs.albumtracker.structures.Album;
import org.collegelabs.albumtracker.structures.ExceptionWrapper;
import org.collegelabs.albumtracker.structures.LastfmError;
import org.collegelabs.albumtracker.structures.Track;
import org.collegelabs.albumtracker.structures.TrackParser;
import org.collegelabs.library.bitmaploader.caches.DiskCache;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;

public class TrackLoader extends AsyncTaskLoader<ExceptionWrapper<ArrayList<Track>>> {

	private Album mAlbum;
	private DiskCache mDiskCache; //TODO memory leak
	private String mUrlString;
	private URL mUrl;
	
	public TrackLoader(Context context, Album album, DiskCache diskCache) {
		super(context);

		mAlbum = album;
		mDiskCache = diskCache;
		
		try {
			mUrlString = LastfmHelper.getAlbumInfo(getContext(), mAlbum);
			mUrl = new URL(mUrlString);
			
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public ExceptionWrapper<ArrayList<Track>> loadInBackground() {
		try{
			if(BuildConfig.DEBUG) Log.d(Constants.TAG, "[TrackLoader] loading: "+mUrlString);
			
			File f = mDiskCache.getFile(mUrlString);
			if(!f.exists()) downloadUrl(f, mUrl);
		
			ArrayList<Track> data = parse(f);
			return new ExceptionWrapper<ArrayList<Track>>(data);
						
		}catch(Exception e){
			if(BuildConfig.DEBUG){
				Log.e(Constants.TAG, "[TrackLoader] "+e.toString());
				e.printStackTrace();
			}
			
			return new ExceptionWrapper<ArrayList<Track>>(e);	
		}
	}

	
	private ArrayList<Track> parse(File file) throws Exception{

		try{
			if(BuildConfig.DEBUG) Log.d(Constants.TAG, "[TrackLoader] parsing");
			
			
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			TrackParser mHandler = new TrackParser();
			xr.setContentHandler(mHandler);		   
			xr.parse(new InputSource(new BufferedInputStream(new FileInputStream(file))));
	
			LastfmError error = mHandler.getException();
			if(error != null){
				throw error;
			}

			return mHandler.getTracks();
			
		}catch(Exception e){
			Map<String,String> extras = new HashMap<String,String>(); 
			extras.put("album", mAlbum.toString());
			extras.put("url", mUrlString);
			
			BugSenseHandler.log("TrackLoader-Parse", extras, e);
			
			throw e;
		}
	}
	
	private void downloadUrl(File file, URL url) throws IOException{
		if(BuildConfig.DEBUG) Log.d(Constants.TAG, "[TrackLoader] downloading");
		
		if(!file.exists()){ 
			file.createNewFile();
		}

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		try{
			InputStream is = null;
			
			if(connection.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST){
				is = connection.getErrorStream();
			}else{
				is = connection.getInputStream();
			}
			
			BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));
			Utils.copyStream(is,os); 

			os.flush();
			os.close();	
			is.close();

		}finally{
			connection.disconnect();
		}

	}
}
