package org.collegelabs.albumtracker.fragments;

import java.util.Date;

import org.collegelabs.albumtracker.Constants;
import org.collegelabs.albumtracker.R;
import org.collegelabs.albumtracker.activities.BaseActivity;
import org.collegelabs.albumtracker.activities.DetailAlbumView;
import org.collegelabs.albumtracker.activities.MainActivity;
import org.collegelabs.albumtracker.content.AlbumProvider;
import org.collegelabs.albumtracker.structures.Album;
import org.collegelabs.albumtracker.structures.AlbumAdapter;
import org.collegelabs.albumtracker.structures.Artist;
import org.collegelabs.library.bitmaploader.BitmapLoader;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

public class AlbumGrid extends Fragment implements OnClickListener, OnItemClickListener, SyncStatusObserver, LoaderCallbacks<Cursor> {

	public static final String BUNDLE_EMPTY_MSG = "msg";
	public static final String BUNDLE_QUERY = "query";
	
	public enum Query{
		All,
		Starred,
		New
	}
	
	private static final int LOADER_ID = 0;
	
	private BitmapLoader bitmapLoader = null;
	private Handler uiHandler = new Handler();
	
	private Query mQuery = Query.All;
	private String mEmptyText = "No Albums";
	private Object syncObserverHandle = null;

	private GridView mGridView;
	private AlbumAdapter mAdapter;
	private TextView emptyText;
	
	
	@Override 
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		syncObserverHandle = ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE | ContentResolver.SYNC_OBSERVER_TYPE_PENDING, this);
	}
	
	
	/*
	 * This can get called more than once, checkout http://developer.android.com/guide/topics/fundamentals/fragments.html
	 */
	@Override 
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
		Bundle args = getArguments();
		if(args!=null){
			String q = args.getString(BUNDLE_QUERY);
			if(q!=null) mQuery = Query.valueOf(q);
		}
		
		updateEmptyText(getView());
		
		BaseActivity activity = ((BaseActivity) getActivity());
		
		//TODO if this happens, then we are likely leaking the previous activity
		//until we destroy the loader that is
		if(bitmapLoader == null) bitmapLoader = new BitmapLoader(activity, activity.getBitmapCache());

		View view = getView();
		
		mGridView = (GridView) view.findViewById(R.id.gridview);

		//Sending a 0 flag will prevent the adapter from registering a content observer.
		//If we didn't pass 0 then this whole context gets leaked because we register 
		//our own observer with the Loader
		mAdapter = new AlbumAdapter(getActivity(), null, 0, bitmapLoader);
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(this);

		getLoaderManager().initLoader(LOADER_ID, null, this);
	}
	
	private void updateGridVisibility(){
		if(mAdapter.getCount() == 0){
			mGridView.setVisibility(View.GONE);	
			emptyText.setText(mEmptyText);
		}else if(mGridView.getVisibility() != View.VISIBLE){
			mGridView.setVisibility(View.VISIBLE);	
		}
	}
	
	private void updateEmptyText(View viewGroup){
		
		switch(mQuery){
    	default:
    	case All:
    		mEmptyText = "No Albums found, please start sync";
    		break;
    	case Starred:
    		mEmptyText = "No starred albums";    		
    		break;
    	case New:
    		mEmptyText = "No new albums";
    		break;
    	}
		
		if(viewGroup == null) return;
		
		//Sync button is only visible in the All tab
		View btn = viewGroup.findViewById(R.id.button_sync);
		btn.setOnClickListener(this);
		btn.setVisibility(mQuery == Query.All ? View.VISIBLE : View.GONE);
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.fragment_albumgrid, null); 
		emptyText = (TextView) v.findViewById(R.id.empty_text);
		//setup onClick listeners
		updateEmptyText(v);
		return v; 
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		
		if(syncObserverHandle!=null) ContentResolver.removeStatusChangeListener(syncObserverHandle);
		if(bitmapLoader!=null) bitmapLoader.shutdownNow();
	}
	
	@Override
	public void onClick(View v){
		switch(v.getId()){
		case R.id.button_sync:

			((MainActivity)getActivity()).syncAccounts();
			
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		Cursor c = (Cursor) mAdapter.getItem(position);
		int colArtistName = c.getColumnIndexOrThrow(AlbumProvider.Album.Albums.ARTIST_NAME);
		int colMBid = c.getColumnIndexOrThrow(AlbumProvider.Album.Albums.ARTIST_MBID);

		int colAlbumName = c.getColumnIndexOrThrow(AlbumProvider.Album.Albums.ALBUM_NAME);
		int colRelease = c.getColumnIndexOrThrow(AlbumProvider.Album.Albums.ALBUM_RELEASE_DATE);
		int colImg = c.getColumnIndexOrThrow(AlbumProvider.Album.Albums.ALBUM_IMG_XLARGE);
		int colID = c.getColumnIndexOrThrow(AlbumProvider.Album.Albums.ALBUM_ID);
		int colStarred = c.getColumnIndexOrThrow(AlbumProvider.Album.Albums.ALBUM_STARRED);
		int colNew = c.getColumnIndexOrThrow(AlbumProvider.Album.Albums.ALBUM_NEW);
		
		final Context context = getActivity();
		
		Artist artist = new Artist(c.getString(colArtistName), c.getString(colMBid), "");
		final Album album = new Album();
		album.ID = c.getInt(colID);
		album.artist = artist;
		album.name = c.getString(colAlbumName);
		album.release = new Date(c.getLong(colRelease));
		album.img_xlarge = c.getString(colImg);
		album.isNew = c.getInt(colNew) == 1;
		album.isStarred = c.getInt(colStarred) == 1;
		
		Intent i = new Intent(context,DetailAlbumView.class);
		i.putExtra("album", album);
		startActivity(i);
	}

	private void onStatusChangedOnUI(int status){
		if(mAdapter != null && mAdapter.getCount() > 0) return;
		
		final View view = getView();
		
		if(view == null) return; //fragment was removed?
		
		final TextView textview = (TextView) view.findViewById(R.id.empty_text);
		final View progressSpinner = view.findViewById(R.id.progressBar1);
		
		final boolean isSyncing;
		
		switch(status){
		case ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE:
			textview.setText("Sync in progress");
			isSyncing = true;
			break;
		case ContentResolver.SYNC_OBSERVER_TYPE_PENDING:
			textview.setText("Waiting for sync");
			isSyncing = true;
			break;
		default:
			isSyncing = false;
			break;
		}
		
		progressSpinner.setVisibility(isSyncing ? View.VISIBLE : View.GONE);
	}
	
	@Override
	public void onStatusChanged(final int status) {
		
		//We get notified on every status change, regardless 
		//if it is our cursor or not, so we check to see if its ours
		
		Context ctx = getActivity();
		if(ctx == null) return;
		
		//notify if sync is not pending
		boolean shouldNotify = (status != ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE
							 && status != ContentResolver.SYNC_OBSERVER_TYPE_PENDING);
			
		AccountManager manager = AccountManager.get(ctx);
		Account[] accounts = manager.getAccountsByType(Constants.ACCOUNT_TYPE);
		//notify if we have a sync pending
		for(Account account : accounts){
			if(ContentResolver.isSyncActive(account, AlbumProvider.AUTHORITY)
			|| ContentResolver.isSyncPending(account, AlbumProvider.AUTHORITY)){
			
				shouldNotify = true;
			}
		}
		
		if(!shouldNotify) return;
		
		//This method is never going to run on the UI thread, so post these back to the fragment's handler
		uiHandler.post(new Runnable(){ 
			@Override public void run() {
				onStatusChangedOnUI(status);
			}
		});
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		
		String selection = null;
		String[] selectionArgs = null;
		
		switch(mQuery){
		case Starred:
			selection = AlbumProvider.Album.Albums.ALBUM_STARRED+" = ?";
			selectionArgs = new String[] {"1"};
			break;
		case New:
			selection = AlbumProvider.Album.Albums.ALBUM_NEW+" = ?";
			selectionArgs = new String[] {"1"};
			break;
		case All:
		default:
			break;
		}
		
		String sort = AlbumProvider.Album.Albums.ALBUM_RELEASE_DATE + " DESC";
	
		//Sneaky framework calls up to the application's context, no need to do it ourselves
		//otherwise using the activity's context would cause a leak
		return new CursorLoader(getActivity(),  AlbumProvider.Album.Albums.CONTENT_URI, null, selection, selectionArgs, sort);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.swapCursor(data); //The loader framework calls close, so we need to swapCursor not changeCursor
		updateGridVisibility();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
        updateGridVisibility();
	}
	
}
