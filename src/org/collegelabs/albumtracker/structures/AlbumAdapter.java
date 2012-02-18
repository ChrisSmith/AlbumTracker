package org.collegelabs.albumtracker.structures;

import org.collegelabs.albumtracker.R;
import org.collegelabs.albumtracker.content.AlbumProvider;
import org.collegelabs.library.bitmaploader.BitmapLoader;
import org.collegelabs.library.bitmaploader.views.AsyncImageView;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AlbumAdapter extends CursorAdapter {

	private BitmapLoader mBitmapLoader;
	
	private int colAlbumName = -1;
//	private int colRelease = -1;
	private int colImg = -1;
	private int colArtistName = -1;
	
	private Bitmap defaultAlbumBitmap;
	
	public AlbumAdapter(Context context, Cursor cursor, int flags, BitmapLoader bitmapLoader) {
		super(context,cursor,flags); 
		mBitmapLoader = bitmapLoader;
		loadColumnIndexes(cursor);
		
		Resources resources = context.getResources();
		defaultAlbumBitmap = BitmapFactory.decodeResource(resources, R.drawable.album_cover);
	}

	private void loadColumnIndexes(Cursor cursor){
		if(cursor != null){
			colAlbumName = cursor.getColumnIndexOrThrow(AlbumProvider.Album.Albums.ALBUM_NAME);
//			colRelease = cursor.getColumnIndexOrThrow(AlbumProvider.Album.Albums.ALBUM_RELEASE_DATE);
			colImg = cursor.getColumnIndexOrThrow(AlbumProvider.Album.Albums.ALBUM_IMG_XLARGE);
			colArtistName = cursor.getColumnIndexOrThrow(AlbumProvider.Album.Albums.ARTIST_NAME);
		}
	}
	
	@Override
	public void changeCursor (Cursor cursor){
		super.changeCursor(cursor);
		loadColumnIndexes(cursor);
	}
	
	@Override
	public Cursor swapCursor (Cursor newCursor){
		loadColumnIndexes(newCursor);
		return super.swapCursor(newCursor);
	}
	
	@Override
	public void bindView(View convertView, Context context, Cursor cursor) {
		AlbumHolder holder = (AlbumHolder) convertView.getTag();
		
		holder.albumName.setText(cursor.getString(colAlbumName));		
		holder.artistName.setText(cursor.getString(colArtistName));
		holder.albumArtwork.setImageUrl(cursor.getString(colImg), mBitmapLoader);		
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inf = LayoutInflater.from(context);
		View view = inf.inflate(R.layout.row_album, null);
		AlbumHolder holder = new AlbumHolder();
		
		holder.albumName = (TextView) view.findViewById(R.id.album_name);
		holder.artistName = (TextView) view.findViewById(R.id.album_artist);
		holder.albumArtwork = (AsyncImageView) view.findViewById(R.id.album_artwork);
		holder.albumArtwork.setDefaultBitmap(defaultAlbumBitmap);
		
		view.setTag(holder);
		
		return view;
	}
}