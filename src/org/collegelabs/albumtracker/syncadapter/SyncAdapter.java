/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.collegelabs.albumtracker.syncadapter;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.collegelabs.albumtracker.LastfmHelper;
import org.collegelabs.albumtracker.R;
import org.collegelabs.albumtracker.activities.MainActivity;
import org.collegelabs.albumtracker.authenticator.AuthenticatorActivity;
import org.collegelabs.albumtracker.content.AlbumProvider;
import org.collegelabs.albumtracker.fragments.AlbumGrid.Query;
import org.collegelabs.albumtracker.structures.Album;
import org.collegelabs.albumtracker.structures.AlbumXmlParser;
import org.collegelabs.albumtracker.structures.LastfmError;
import org.collegelabs.albumtracker.structures.LogFile;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.ParseException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * SyncAdapter implementation for syncing sample SyncAdapter contacts to the
 * platform ContactOperations provider.  This sample shows a basic 2-way
 * sync between the client and a sample server.  It also contains an
 * example of how to update the contacts' status messages, which
 * would be useful for a messaging or social networking client.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

	public static final String TAG = "SyncAdapter";
	private static final String SYNC_MARKER_KEY = "com.example.android.samplesync.marker";
	private static final boolean NOTIFY_AUTH_FAILURE = true;

	private final AccountManager mAccountManager;

	private final Context mContext;

	public SyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		mContext = context;
		mAccountManager = AccountManager.get(context);
	}


	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) { 

		//Create a unique notification per account
		final int notificationId = account.hashCode();

		LogFile log = new LogFile(mContext);

		try {
			// see if we already have a sync-state attached to this account. By handing
			// This value to the server, we can just get the contacts that have
			// been updated on the server-side since our last sync-up
			long lastSyncMarker = getServerSyncMarker(account);

			// Update the local contacts database with the changes. updateContacts()
			// returns a syncState value that indicates the high-water-mark for
			// the changes we received.
			//            log.write( "Calling AlbumProvider's sync contacts");
			long newSyncState = System.currentTimeMillis();

			//load via Internet
			log.write("downloading xml feed");

			URL Url = new URL(LastfmHelper.GetNewReleases(mContext, account.name));
			InputStream is = null;
			AlbumXmlParser mHandler = null;
			
			HttpURLConnection connection = (HttpURLConnection) Url.openConnection();

			try{
				
				if(connection.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST){
					is = connection.getErrorStream();
				}else{
					is = connection.getInputStream();
				}
				
				//parse
				log.write("Parsing");
				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();
				mHandler = new AlbumXmlParser();
				xr.setContentHandler(mHandler);		   
				xr.parse(new InputSource(is));
				is.close();

			}finally{
				connection.disconnect();
			}

			LastfmError error = mHandler.getException();
			if(error == null){

				ArrayList<Album> albums = mHandler.getListAlbums();

				log.write("size: "+albums.size());

				log.write("Saving");

				ContentResolver resolver = getContext().getContentResolver();
				Uri albumUri = AlbumProvider.Album.Albums.CONTENT_URI;
				ContentValues values = new ContentValues();
				Cursor c = null;

				int newAlbums = 0;
				Album firstNewAlbum = null;


				for(Album album : albums){
					try{
						//Check against the Album Name, Release Date and Artist Id
						String[] projection = { AlbumProvider.Album.Albums.ALBUM_ID, AlbumProvider.Album.Albums.ALBUM_RELEASE_DATE };
						String where =  AlbumProvider.Album.Albums.ALBUM_NAME +" = ?"
						+ " AND "+AlbumProvider.Album.Albums.ARTIST_NAME+" like ?"; //case ignore on the name

						final long trunk = 1000 * 60 * 60; //truncate by the day
						final long truncatedAlbumTime = album.release.getTime()/trunk * trunk;

						log.write("org time: "+album.release.getTime());
						log.write("new time: "+ truncatedAlbumTime);
						Date d =  new Date(truncatedAlbumTime);
						log.write("new date: "+d.toGMTString());

						String[] selectionArgs = { album.name, album.artist.name };

						c = resolver.query(albumUri, projection, where, selectionArgs, null);
						if(c.moveToFirst()){
							log.write("Album exists : "+album.name);

							long oldDate = c.getLong(c.getColumnIndexOrThrow(AlbumProvider.Album.Albums.ALBUM_RELEASE_DATE));

							//check the see if the date changed
							if(oldDate != truncatedAlbumTime){
								log.write("Updated Album releasedate : "+album.name
										+" old time: "+ new Date(oldDate).toGMTString()
										+" new time: "+new Date(truncatedAlbumTime).toGMTString());

								int columnIndex = c.getColumnIndexOrThrow(AlbumProvider.Album.Albums.ALBUM_ID);

								values.clear();
								values.put(AlbumProvider.Album.Albums.ALBUM_RELEASE_DATE, truncatedAlbumTime);
								resolver.update(albumUri, values, AlbumProvider.Album.Albums.ALBUM_ID+" = ?", 
										new String[] {""+c.getInt(columnIndex)});                    	    		
							}

						}else{
							values.clear();
							values.put(AlbumProvider.Album.Albums.ARTIST_NAME, album.artist.name);
							values.put(AlbumProvider.Album.Albums.ARTIST_MBID, album.artist.mbid);
							values.put(AlbumProvider.Album.Albums.ARTIST_URL, album.artist.url);
							values.put(AlbumProvider.Album.Albums.ALBUM_IMG_SMALL, album.img_small);
							values.put(AlbumProvider.Album.Albums.ALBUM_IMG_MEDIUM, album.img_medium);
							values.put(AlbumProvider.Album.Albums.ALBUM_IMG_LARGE, album.img_large);
							values.put(AlbumProvider.Album.Albums.ALBUM_IMG_XLARGE, album.img_xlarge);
							values.put(AlbumProvider.Album.Albums.ALBUM_NAME, album.name);
							values.put(AlbumProvider.Album.Albums.ALBUM_MBID, album.mbid);
							values.put(AlbumProvider.Album.Albums.ALBUM_RELEASE_DATE, truncatedAlbumTime);
							values.put(AlbumProvider.Album.Albums.ALBUM_URL, album.url);
							Uri resultUri = resolver.insert(albumUri, values); 
							int albumId = Integer.parseInt(resultUri.getLastPathSegment());
							album.ID = albumId;

							if(newAlbums==0) firstNewAlbum = album;
							newAlbums++;

							log.write("Inserted Album : "+album.name+" id: "+album.ID);
							log.write(album.toString());
							Intent intent = new Intent(mContext,BackgroundService.class);
							intent.setAction(BackgroundService.ACTION_DOWNLOAD_IMAGE);
							intent.putExtra(BackgroundService.EXTRA_URL, album.img_xlarge);
							mContext.startService(intent);

							intent = new Intent(mContext,BackgroundService.class);
							intent.setAction(BackgroundService.ACTION_DOWNLOAD_BUYLINKS);
							intent.putExtra(BackgroundService.EXTRA_ALBUM, album);
							mContext.startService(intent);
						}
					}finally{
						if(c!=null) c.close();
						c = null;
					}
				}

				if(newAlbums > 0){
					//notify if enabled
					createNotification(newAlbums, firstNewAlbum, notificationId);
				}

				log.write("Done");

				// Save off the new sync marker. On our next sync, we only want to receive
				// contacts that have changed since this sync...
				setServerSyncMarker(account, newSyncState);

			}else{ //exceptions from last.fm
				createErrorNotification(account, error.getErrorCode(), error.getErrorMessage(), notificationId);
				log.write("Error: "+error.getErrorCode()+" : "+error.getErrorMessage());
			}


		} catch (final IOException e) {
			log.write(e);
			Log.e(TAG, "IOException", e);
			syncResult.stats.numIoExceptions++;
		} catch (final ParseException e) {
			log.write(e);
			Log.e(TAG, "ParseException", e);
			syncResult.stats.numParseExceptions++;
		} catch (SAXException e) {
			log.write(e);
			Log.e(TAG, "SAXException", e);
			syncResult.stats.numParseExceptions++;
		} catch (ParserConfigurationException e) {
			log.write(e);
			Log.e(TAG, "ParserConfigurationException", e);
			syncResult.stats.numParseExceptions++;
		}finally{
			log.close();
		}

	}

	private static final int ERR_BAD_USER = 6;

	/**
	 * This helper function fetches the last known high-water-mark
	 * we received from the server - or 0 if we've never synced.
	 * @param account the account we're syncing
	 * @return the change high-water-mark
	 */
	private long getServerSyncMarker(Account account) {
		String markerString = mAccountManager.getUserData(account, SYNC_MARKER_KEY);
		if (!TextUtils.isEmpty(markerString)) {
			return Long.parseLong(markerString);
		}
		return 0;
	}

	/**
	 * Save off the high-water-mark we receive back from the server.
	 * @param account The account we're syncing
	 * @param marker The high-water-mark we want to save.
	 */
	private void setServerSyncMarker(Account account, long marker) {
		mAccountManager.setUserData(account, SYNC_MARKER_KEY, Long.toString(marker));
	}



	//    private int NOTIFICATION_ID = 1;
	public static final String PREF_SOUND = "pref_sound";
	public static final String PREF_VIBRATE = "pref_vibrate";
	public static final String PREF_LED = "pref_led";

	private Notification getNotification(CharSequence tickerText){
		SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(mContext); 
		boolean mSound = config.getBoolean(PREF_SOUND, true);
		boolean mVibrate = config.getBoolean(PREF_VIBRATE, true);
		boolean mLed = config.getBoolean(PREF_LED, true);

		int icon = R.drawable.icon;

		Notification notification = new Notification(icon, tickerText, System.currentTimeMillis());
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		if(mSound) notification.defaults |= Notification.DEFAULT_SOUND;
		if(mVibrate) notification.defaults |= Notification.DEFAULT_VIBRATE;
		if(mLed) notification.defaults |= Notification.DEFAULT_LIGHTS;

		return notification;
	}


	private void createErrorNotification(Account account, int errorCode, String message, int notificationId){
		CharSequence tickerText = "Update Failed, code: "+errorCode;

		Notification notification = getNotification(tickerText);

		CharSequence contentTitle = tickerText;
		CharSequence contentText = message;		

		Intent notificationIntent = new Intent();

		switch(errorCode){
		case ERR_BAD_USER:{ //Invalid Params (user)
			AccountManager manager = AccountManager.get(mContext);
			manager.removeAccount(account, null, null);
			//Open the login page
			notificationIntent = new Intent(mContext, AuthenticatorActivity.class);
		}break;

		default:
			break;
		}


		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);

		notification.setLatestEventInfo(mContext, contentTitle, contentText, contentIntent);

		NotificationManager mNotificationManager = 
			(NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

		mNotificationManager.notify(notificationId, notification);
	}

	private void createNotification(int count, Album firstNewAlbum, int notificationId){
		CharSequence tickerText = "New Album Releases";

		Notification notification = getNotification(tickerText);

		CharSequence contentTitle = tickerText;
		CharSequence contentText;

		if(count == 1){
			contentText = firstNewAlbum.name + " by "+firstNewAlbum.artist.name+ " is available";
		}else{
			contentText = count+" new albums";			
		}

		Intent notificationIntent = new Intent(mContext, MainActivity.class);
		notificationIntent.putExtra(MainActivity.BUNDLE_QUERY, Query.New.toString());

		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);

		notification.setLatestEventInfo(mContext, contentTitle, contentText, contentIntent);

		NotificationManager mNotificationManager = 
			(NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

		mNotificationManager.notify(notificationId, notification);
	}

}
