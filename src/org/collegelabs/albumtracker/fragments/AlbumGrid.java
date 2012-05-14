package org.collegelabs.albumtracker.fragments;

import org.collegelabs.albumtracker.BuildConfig;
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
import android.os.Message;
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

import com.actionbarsherlock.app.SherlockFragment;

public class AlbumGrid extends SherlockFragment implements OnClickListener, OnItemClickListener, LoaderCallbacks<Cursor> {

	public static final String BUNDLE_EMPTY_MSG = "msg";
	public static final String BUNDLE_QUERY = "query";
	private static final int SYNC_STATUS_CHANGED = 1;
	
	
	public enum Query{
		All,
		Starred,
		New
	}
	
	private static final int LOADER_ID = 0;
	
	private BitmapLoader bitmapLoader = null;
	private Handler uiHandler = new Handler(){
		@Override
		public void handleMessage (Message msg){
			if(msg.what == SYNC_STATUS_CHANGED)
				onStatusChangedOnUI(msg.arg1);
		}
	};
	
	private Query mQuery = Query.All;
	private String mEmptyText = "No Albums";
	private Object syncObserverHandle = null;

	private GridView mGridView;
	private AlbumAdapter mAdapter;
	private TextView emptyText;
	private MySyncStatusObserver mSyncObserver;
	
	@Override 
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
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
				
		//We really only need to display the spinner on the first tab
		if(syncObserverHandle == null && mQuery.equals(Query.All)){
			mSyncObserver = new MySyncStatusObserver(getActivity(), uiHandler);
			
			syncObserverHandle = ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE 
					| ContentResolver.SYNC_OBSERVER_TYPE_PENDING, mSyncObserver);
		}
		
		updateEmptyText(getView());
		
		BaseActivity activity = ((BaseActivity) getActivity());
		
		if(bitmapLoader == null){ //Fragment can be re-attached to the activity, no need to start/stop the loader
			bitmapLoader = new BitmapLoader(activity, activity.getBitmapCache(), activity.getBitmapCachePolicy());
		}

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
		
		if(syncObserverHandle!=null){
			ContentResolver.removeStatusChangeListener(syncObserverHandle);
			mSyncObserver.onDestroy(); //Have to remove references to the context, otherwise it will leak
		}
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
		
		final Context context = getActivity();
		
		final Artist artist = new Artist(c);
		final Album album = new Album(c);
		album.artist = artist;
		
		if(BuildConfig.DEBUG)
			Log.d(Constants.TAG,"onClick: "+album.toString());
			
		
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
		
		final View btn = view.findViewById(R.id.button_sync);
		btn.setVisibility(isSyncing ? View.GONE : View.VISIBLE);
	}
	
	static class MySyncStatusObserver implements SyncStatusObserver{
	
		private Context mContext;
		private Handler mHandler;
		
		public MySyncStatusObserver(Context context, Handler handler){
			mContext = context;
			mHandler = handler;
		}
		
		public void onDestroy(){
			mContext = null;
			mHandler = null;
		}
		
		@Override
		public void onStatusChanged(final int status) {
			
			if(mContext == null || mHandler == null){ //Shouldn't ever happen
				Log.e(Constants.TAG, "SyncStatusObserver is still active after being unattached");
				return;
			}
			
			//We get notified on every status change, regardless 
			//if it is our cursor or not, so we check to see if its ours
			//notify if sync is not pending
			boolean shouldNotify = (status != ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE
								 && status != ContentResolver.SYNC_OBSERVER_TYPE_PENDING);
				
			AccountManager manager = AccountManager.get(mContext);
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
			mHandler.sendMessage(mHandler.obtainMessage(SYNC_STATUS_CHANGED, status, 0));			
		}
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
			selection = AlbumProvider.Album.Albums.ALBUM_VISIBLE+" = ?";
			selectionArgs = new String[] {"1"};
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
