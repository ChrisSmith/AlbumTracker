package org.collegelabs.albumtracker.fragments;

import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.collegelabs.albumtracker.BuildConfig;
import org.collegelabs.albumtracker.Constants;
import org.collegelabs.albumtracker.R;
import org.collegelabs.albumtracker.activities.DetailAlbumView;
import org.collegelabs.albumtracker.content.AlbumProvider;
import org.collegelabs.albumtracker.structures.Album;
import org.collegelabs.albumtracker.structures.ParseBuyLinksRunnable;
import org.collegelabs.library.bitmaploader.BitmapLoader;
import org.collegelabs.library.bitmaploader.views.AsyncImageView;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

public class ArtworkFragment extends SherlockFragment {

	private TextView artistName, albumName, releaseDate;
	private BitmapLoader bitmapLoader = null;
	private RelativeLayout container; 
	private Album album;
	
	private Cursor mBuyLinksCursor = null;
	private AsyncTask<?,?,?> mLoaderTask;
	
	@Override
	public void onActivityCreated(Bundle bundle){
		super.onActivityCreated(bundle);
		
		View v = getView();
		
		albumName = (TextView) v.findViewById(R.id.detail_album_title);
		artistName = (TextView) v.findViewById(R.id.detail_artist_name);
		releaseDate = (TextView) v.findViewById(R.id.detail_artist_release);

		album = (Album) getArguments().getParcelable("album");

		albumName.setText(album.name);
		artistName.setText(album.artist.name);
		DateFormat outputFormat = new SimpleDateFormat("MMM dd");

		releaseDate.setText(outputFormat.format(album.release));

		Display display = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

		int width = display.getWidth();
		int height = display.getHeight();
		int spec = (width < height) ? width : height;
		final double eigth = spec / 8.0 ;

		albumName.setWidth((int) (eigth * 7));

		artistName.post(new Runnable(){ //will run after the view has been laid out and params filled
			@Override
			public void run() {
				int nWidth = (int) (eigth * 5);
				if(artistName.getWidth() < nWidth){
					artistName.setWidth(nWidth);			
				}
			}
		});


		artistName.post(new Runnable(){
			@Override
			public void run() {
				int nWidth = (int) (eigth * 2);
				if(releaseDate.getWidth() < nWidth){
					releaseDate.setWidth(nWidth);			
				}
			}
		});

		AnimationSet set = new AnimationSet(true);
		
		Animation animation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, -1.0f,Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f,Animation.RELATIVE_TO_SELF, 0.0f
		);
		animation.setDuration(150);
		set.addAnimation(animation);

		LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);
		container = ((RelativeLayout) v.findViewById(R.id.detail_view_anim_set)); 
		container.setLayoutAnimation(controller);

		Resources resources = getResources();
		Bitmap defaultAlbumBitmap = new BitmapDrawable(resources,BitmapFactory.decodeResource(resources, R.drawable.album_cover)).getBitmap();
		
		AsyncImageView albumArtwork = (AsyncImageView) v.findViewById(R.id.album_artwork_large);
		albumArtwork.setDefaultBitmap(defaultAlbumBitmap);
		
		final DetailAlbumView host = ((DetailAlbumView) getActivity());
		
		Bitmap b = host.getBitmapCache().get(album.img_xlarge);
		
		if(b!=null){
			albumArtwork.setImageBitmap(b);
		}else{
			bitmapLoader = new BitmapLoader(host, host.getBitmapCache(), host.getBitmapCachePolicy());
			albumArtwork.setImageUrl(album.img_xlarge, bitmapLoader);		
		}
		
		
		final Button buyButton = (Button) v.findViewById(R.id.button_buy_button);
		final ContentResolver resolver = getActivity().getContentResolver();
		
		mLoaderTask = new AsyncTask<Void,Void,Cursor>(){
			@Override
			protected Cursor doInBackground(Void... params) {

				Uri uri = AlbumProvider.AffiliateLink.AffiliateLinks.CONTENT_URI;
				String where = AlbumProvider.AffiliateLink.AffiliateLinks.AFF_ALBUM_ID+" = ?";
				String[] whereArgs = { ""+album.ID};
				String orderBy = AlbumProvider.AffiliateLink.AffiliateLinks.AFF_SUPPLIER_NAME;
				Cursor c = resolver.query(uri, null, where, whereArgs, orderBy);

				if(c.getCount() == 0){
					c.close();
					new ParseBuyLinksRunnable(getActivity(), album).run();
					c = resolver.query(uri, null, where, whereArgs, orderBy);
				}
				
				return c;
			}
			
			@Override
			protected void onPostExecute(Cursor cursor){
				buyButton.setVisibility(View.VISIBLE);
				mBuyLinksCursor = cursor;
			}
			
		}.execute();
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View view = inflater.inflate(R.layout.fragment_album_artwork, null);
		return view;
	}
	
	public void onClickBuyButton(final Context ctx){
		if(mBuyLinksCursor == null){
			Toast.makeText(ctx,"Waiting for purchase links", Toast.LENGTH_SHORT).show();
			return;
		}else if(mBuyLinksCursor.getCount() == 0){
			Toast.makeText(ctx,"No links found for purchase", Toast.LENGTH_SHORT).show();
			return;
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle("Buy From")
		.setCursor(mBuyLinksCursor, 
			new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(BuildConfig.DEBUG) Log.d(Constants.TAG,"selected: "+which);
					mBuyLinksCursor.moveToPosition(which);
					
					int urlIndex = mBuyLinksCursor.getColumnIndexOrThrow(AlbumProvider.AffiliateLink.AffiliateLinks.AFF_BUY_LINK);
					try {
						Intent i = Intent.parseUri(mBuyLinksCursor.getString(urlIndex), 0);
						startActivity(i);
					} catch (URISyntaxException e) {
						Toast.makeText(ctx,"Failed to open url", Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					}
				}
			}, 
			AlbumProvider.AffiliateLink.AffiliateLinks.AFF_SUPPLIER_NAME)
		.show();

	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		if(mLoaderTask!=null) mLoaderTask.cancel(true);
		if(bitmapLoader!=null) bitmapLoader.shutdownNow();
		if(mBuyLinksCursor!=null) mBuyLinksCursor.close();
	}
}
