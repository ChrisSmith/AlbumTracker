package org.collegelabs.albumtracker.fragments;

import java.util.ArrayList;
import java.util.List;

import org.collegelabs.albumtracker.BuildConfig;
import org.collegelabs.albumtracker.Constants;
import org.collegelabs.albumtracker.R;
import org.collegelabs.albumtracker.loaders.TrackLoader;
import org.collegelabs.albumtracker.structures.Album;
import org.collegelabs.albumtracker.structures.ExceptionWrapper;
import org.collegelabs.albumtracker.structures.Track;
import org.collegelabs.library.bitmaploader.caches.DiskCache;
import org.collegelabs.library.bitmaploader.caches.SimpleLruDiskCache;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

public class TrackListFragment extends SherlockListFragment implements LoaderCallbacks<ExceptionWrapper<ArrayList<Track>>>{

	private DiskCache mDiskCache;
	private Album mAlbum;
	private ListView mListView;
	private TrackAdapter mAdapter;
	
	@Override 
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
		mDiskCache = new SimpleLruDiskCache(getActivity());
		
		Bundle args = getArguments();
		if(args == null) throw new IllegalArgumentException("must provide an album in arguments");

		mAdapter = new TrackAdapter(getActivity(), new ArrayList<Track>());
		mListView = getListView();
		mListView.setAdapter(mAdapter);
		
		Loader<?> l = getActivity().getSupportLoaderManager().initLoader(0, args, this);
		l.forceLoad();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		
		if(mDiskCache != null) mDiskCache.disconnect();
	}

	@Override
	public Loader<ExceptionWrapper<ArrayList<Track>>> onCreateLoader(int loaderId, Bundle bundle) {
		mAlbum = bundle.getParcelable("album");
		if(mAlbum == null) throw new IllegalArgumentException("must provide an album");
		
		setListShown(false);
		
		return new TrackLoader(getActivity(), mAlbum, mDiskCache);
	}

	@Override
	public void onLoadFinished(Loader<ExceptionWrapper<ArrayList<Track>>> loader, ExceptionWrapper<ArrayList<Track>> wrapper) {
		
		if(BuildConfig.DEBUG) Log.d(Constants.TAG, "[TLFragment] onLoadFinished, error: "+wrapper.hasException());
		
		mAdapter.clear();
		
		setListShown(true);
		
		if(!wrapper.hasException()){
			ArrayList<Track> tracks = wrapper.getData();
			for(Track track : tracks){
				mAdapter.add(track);
			}
		}else{
			setEmptyText(wrapper.getException().toString());
		}
	
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<ExceptionWrapper<ArrayList<Track>>> loader) {
		mAdapter.clear();
		mAdapter.notifyDataSetChanged();
	}
	
	private static class TrackHolder{
		TextView trackName, trackTime, trackArtist;
	}
	
	private class TrackAdapter extends ArrayAdapter<Track>{
		
		public TrackAdapter(Context context, List<Track> objects){
			super(context, R.layout.row_track, objects);
		}
		
		public View getView (int position, View convertView, ViewGroup parent){
			if(convertView == null) convertView = getNewView();
			
			TrackHolder holder = (TrackHolder) convertView.getTag();
			Track track = getItem(position);
			holder.trackName.setText(track.getName());
			holder.trackArtist.setText(track.getArtist().name);
			holder.trackTime.setText(track.getFormattedLength());
			
			return convertView;
		}
		
		private View getNewView(){
			View v = LayoutInflater.from(getContext()).inflate(R.layout.row_track, null);
			TrackHolder holder = new TrackHolder();
			holder.trackName = (TextView) v.findViewById(R.id.track_name);
			holder.trackArtist = (TextView) v.findViewById(R.id.track_artist);
			holder.trackTime = (TextView) v.findViewById(R.id.track_time);
			
			v.setTag(holder);
			return v;
		}
	}
}
