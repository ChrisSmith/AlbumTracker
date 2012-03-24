package org.collegelabs.albumtracker.content;

import java.util.HashMap;

import org.collegelabs.albumtracker.BuildConfig;
import org.collegelabs.albumtracker.Constants;
import org.collegelabs.albumtracker.content.AlbumProvider.AffiliateLink.AffiliateLinks;
import org.collegelabs.albumtracker.content.AlbumProvider.Album.Albums;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class AlbumProvider extends ContentProvider {

	private static String TAG = Constants.TAG;
	public static final String DATABASE_NAME = "albumprovider.db";
	private static final int DATABASE_VERSION = 1;
	

	public static final String AUTHORITY = "org.collegelabs.albumtracker.content.albumprovider";
	private static final UriMatcher sUriMatcher;
	private static final int ALBUMS = 1;
	private static final int AFF_LINKS = 2;
	private static HashMap<String, String> albumsProjectionMap;
	private static HashMap<String, String> afflinksProjectionMap;	
	
	private static final String ALBUM_TABLE_NAME = "albums";
	private static final String AFFILIATE_LINKS_TABLE_NAME = "affiliatelinks";
	
	/*
	 * Tables
	 */
	private static final String TABLE_ALBUMS_CREATE = 
    	"create table "+ALBUM_TABLE_NAME
		+" ("+Albums.ALBUM_ID+" integer primary key autoincrement, "
	    + Albums.ALBUM_NAME+" string, "
	    + Albums.ALBUM_MBID+" string, "
	    + Albums.ALBUM_URL+" string, "
	    + Albums.ALBUM_IMG_SMALL+" string, "
	    + Albums.ALBUM_IMG_MEDIUM+" string, "
	    + Albums.ALBUM_IMG_LARGE+" string, "
	    + Albums.ALBUM_IMG_XLARGE+" string, "
	    + Albums.ALBUM_RELEASE_DATE+" integer," 
	    + Albums.ARTIST_NAME+" string, "
	    + Albums.ARTIST_MBID+" string, "
	    + Albums.ARTIST_URL+" string," 
	    + Albums.ALBUM_NEW+" integer default 1,"
	    + Albums.ALBUM_STARRED+" integer default 0 );";
	

	
	 private static final String TABLE_AFFILIATE_LINKS = 
		 "create table "+AFFILIATE_LINKS_TABLE_NAME
		 +" ("+AffiliateLinks.AFF_ID+" integer primary key autoincrement, "
		 + AffiliateLinks.AFF_ALBUM_ID+" integer, "
		 + AffiliateLinks.AFF_SUPPLIER_NAME+" text, "
		 + AffiliateLinks.AFF_BUY_LINK+" text, "
		 + AffiliateLinks.AFF_CURRENCY+" text, "
		 + AffiliateLinks.AFF_AMOUNT+" text, "
		 + AffiliateLinks.AFF_IS_SEARCH+" boolean, "
		 + AffiliateLinks.AFF_IS_PHYSICAL+" boolean);";
	 
	 /*
	  * Indexes
	  */
	
	 //No Indexes for now, restore later
//	 private static final String ADD_INDEX_TO_ALBUM_NAME_ARTIST = 
//		 "CREATE UNIQUE INDEX album_name_artist_index ON " + ALBUM_TABLE_NAME +
//		 "("+Albums.ALBUM_NAME+", "+Albums.ARTIST_NAME+");";
//	 
//	 private static final String ADD_INDEX_TO_ALBUM_RELEASE_SORT_DESC = 
//		 "CREATE INDEX album_release_sort_desc_index ON " + ALBUM_TABLE_NAME +
//		 "("+Albums.ALBUM_RELEASE_DATE+" DESC );";
//
//	 private static final String ADD_INDEX_TO_ALBUM_RELEASE_SORT_ASC = 
//		 "CREATE INDEX album_release_sort_asc_index ON " + ALBUM_TABLE_NAME +
//		 "("+Albums.ALBUM_RELEASE_DATE+" ASC );";
//	
//	 private static final String ADD_INDEX_TO_AFFILIATE_LINKS = 
//		 "CREATE INDEX affiliates_to_album ON " + AFFILIATE_LINKS_TABLE_NAME +
//		 "("+AffiliateLinks.AFF_ALBUM_ID+", "+ AffiliateLinks.AFF_SUPPLIER_NAME+");";
	 
	 
	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			if(BuildConfig.DEBUG) Log.i(TAG,"creating database");
			//Tables
			db.execSQL(TABLE_ALBUMS_CREATE);
			db.execSQL(TABLE_AFFILIATE_LINKS);
			
			//Indexes
//			db.execSQL(ADD_INDEX_TO_ALBUM_NAME_ARTIST);
//			db.execSQL(ADD_INDEX_TO_ALBUM_RELEASE_SORT_DESC);
//			db.execSQL(ADD_INDEX_TO_ALBUM_RELEASE_SORT_ASC);
//			db.execSQL(ADD_INDEX_TO_AFFILIATE_LINKS);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if(BuildConfig.DEBUG) Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");	
			db.execSQL("DROP TABLE IF EXISTS "+ALBUM_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS "+AFFILIATE_LINKS_TABLE_NAME);
			onCreate(db);            
		}
	}

	private DatabaseHelper dbHelper;

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case ALBUMS:
			count = db.delete(ALBUM_TABLE_NAME, where, whereArgs);
			break;
		case AFF_LINKS:
			count = db.delete(AFFILIATE_LINKS_TABLE_NAME, where, whereArgs);
			break;
			
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case ALBUMS:
			return Albums.CONTENT_TYPE;

		case AFF_LINKS:
			return AffiliateLinks.CONTENT_TYPE;
			
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}


	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		
		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}
		
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		switch (sUriMatcher.match(uri)) {
		case ALBUMS:{
			long rowId = db.insert(ALBUM_TABLE_NAME, Albums.ALBUM_NAME, values);
			if (rowId > 0) {
				Uri vUri = ContentUris.withAppendedId(Albums.CONTENT_URI, rowId);
				getContext().getContentResolver().notifyChange(vUri, null);
				return vUri;
			}else{
				Log.e(TAG,"failed to insert Albums");
				Log.e(TAG,values.toString());
			}
			
		}break;
				
		case AFF_LINKS:{
			long rowId = db.insert(AFFILIATE_LINKS_TABLE_NAME, AffiliateLinks.AFF_AMOUNT, values);
			if (rowId > 0) {
				Uri vUri = ContentUris.withAppendedId(AffiliateLinks.CONTENT_URI, rowId);
				getContext().getContentResolver().notifyChange(vUri, null);
				return vUri;
			}else{
				Log.e(TAG,"failed to insert Affiliate Link");
				Log.e(TAG,values.toString());
			}
		}break;
		
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseHelper(getContext());
		return true;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		switch (sUriMatcher.match(uri)) {
		case ALBUMS:
			qb.setTables(ALBUM_TABLE_NAME);
			qb.setProjectionMap(albumsProjectionMap);
			break;

		case AFF_LINKS:
			qb.setTables(AFFILIATE_LINKS_TABLE_NAME);
			qb.setProjectionMap(afflinksProjectionMap);			
			break;
			
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case ALBUMS:
			count = db.update(ALBUM_TABLE_NAME, values, where, whereArgs);
			break;
		case AFF_LINKS:
			count = db.update(AFFILIATE_LINKS_TABLE_NAME, values, where, whereArgs);
			break;
			
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		
		sUriMatcher.addURI(AUTHORITY, ALBUM_TABLE_NAME, ALBUMS);
		albumsProjectionMap = new HashMap<String, String>();
		albumsProjectionMap.put(Albums.ALBUM_ID, Albums.ALBUM_ID);
		albumsProjectionMap.put(Albums.ALBUM_NAME, Albums.ALBUM_NAME);
		albumsProjectionMap.put(Albums.ALBUM_MBID, Albums.ALBUM_MBID);
		albumsProjectionMap.put(Albums.ALBUM_URL, Albums.ALBUM_URL);
		albumsProjectionMap.put(Albums.ALBUM_IMG_SMALL, Albums.ALBUM_IMG_SMALL);
		albumsProjectionMap.put(Albums.ALBUM_IMG_MEDIUM, Albums.ALBUM_IMG_MEDIUM);
		albumsProjectionMap.put(Albums.ALBUM_IMG_LARGE, Albums.ALBUM_IMG_LARGE);
		albumsProjectionMap.put(Albums.ALBUM_IMG_XLARGE, Albums.ALBUM_IMG_XLARGE);
		albumsProjectionMap.put(Albums.ALBUM_RELEASE_DATE, Albums.ALBUM_RELEASE_DATE);
		albumsProjectionMap.put(Albums.ALBUM_NEW, Albums.ALBUM_NEW);
		albumsProjectionMap.put(Albums.ALBUM_STARRED, Albums.ALBUM_STARRED);
		
		//artist attributes
		albumsProjectionMap.put(Albums.ARTIST_NAME, Albums.ARTIST_NAME);
		albumsProjectionMap.put(Albums.ARTIST_MBID, Albums.ARTIST_MBID);
		albumsProjectionMap.put(Albums.ARTIST_URL, Albums.ARTIST_URL);
		
		
		sUriMatcher.addURI(AUTHORITY, AFFILIATE_LINKS_TABLE_NAME, AFF_LINKS);
		afflinksProjectionMap = new HashMap<String, String>();
		afflinksProjectionMap.put(AffiliateLinks.AFF_ID, AffiliateLinks.AFF_ID);
		afflinksProjectionMap.put(AffiliateLinks.AFF_ALBUM_ID, AffiliateLinks.AFF_ALBUM_ID);
		afflinksProjectionMap.put(AffiliateLinks.AFF_SUPPLIER_NAME, AffiliateLinks.AFF_SUPPLIER_NAME);
		afflinksProjectionMap.put(AffiliateLinks.AFF_BUY_LINK, AffiliateLinks.AFF_BUY_LINK);
		afflinksProjectionMap.put(AffiliateLinks.AFF_CURRENCY, AffiliateLinks.AFF_CURRENCY);
		afflinksProjectionMap.put(AffiliateLinks.AFF_AMOUNT, AffiliateLinks.AFF_AMOUNT);
		afflinksProjectionMap.put(AffiliateLinks.AFF_IS_SEARCH, AffiliateLinks.AFF_IS_SEARCH);
		afflinksProjectionMap.put(AffiliateLinks.AFF_IS_PHYSICAL, AffiliateLinks.AFF_IS_PHYSICAL);
		
	}
	

	public static class Album {

		public Album() {
		}

		public static final class Albums implements BaseColumns {
			private Albums() {
			}
			
			public static final Uri CONTENT_URI = Uri.parse("content://" + AlbumProvider.AUTHORITY + "/albums");
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.collegelabs.albumtracker.albums";

			public static final String ALBUM_ID = "_id";
			public static final String ALBUM_NAME = "name";
			public static final String ALBUM_RELEASE_DATE = "release";
			public static final String ALBUM_MBID = "mbid";
			public static final String ALBUM_URL = "url";
			public static final String ALBUM_IMG_SMALL = "img_small";
			public static final String ALBUM_IMG_MEDIUM = "img_medium";
			public static final String ALBUM_IMG_LARGE = "img_large";		
			public static final String ALBUM_IMG_XLARGE = "img_xlarge";			
		
			public static final String ARTIST_NAME = "artist_name";
			public static final String ARTIST_MBID = "artist_mbid";
			public static final String ARTIST_URL = "artist_url";
		
			public static final String ALBUM_NEW = "new";	
			public static final String ALBUM_STARRED = "starred";	
			
		}
	}
	
	public static class AffiliateLink {

		public AffiliateLink() {
		}

		public static final class AffiliateLinks implements BaseColumns {
			private AffiliateLinks() {
			}

			public static final Uri CONTENT_URI = Uri.parse("content://" + AlbumProvider.AUTHORITY + "/affiliatelinks");
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.collegelabs.albumtracker.affiliatelinks";

			public static final String AFF_ID = "_id";
			public static final String AFF_ALBUM_ID = "album_id";
			public static final String AFF_SUPPLIER_NAME = "supp_name";
			public static final String AFF_BUY_LINK = "buy_link";
			public static final String AFF_CURRENCY = "currency";
			public static final String AFF_AMOUNT = "amount";
			public static final String AFF_IS_SEARCH = "is_search";
			public static final String AFF_IS_PHYSICAL = "is_physical";
			
		}
	}
}
