package org.collegelabs.albumtracker.structures;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.collegelabs.albumtracker.LastfmHelper;
import org.collegelabs.albumtracker.content.AlbumProvider;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class ParseBuyLinksRunnable implements Runnable {

	private Album mAlbum;
	private Context mContext;
	
	public ParseBuyLinksRunnable(Context ctx, Album album){
		if(album.ID < 0) throw new IllegalArgumentException("Album ID must be > 0");
		mAlbum = album;
		mContext = ctx;
	}
	
	
	@Override
	public void run() {
		
		LogFile log = new LogFile(mContext);

		try {
			URL Url = new URL(LastfmHelper.getBuyLinks(mContext, mAlbum));
			InputStream is = Url.openStream();
			//			File input = new File("/mnt/sdcard/Download/lastfm/links.xml");
			//			BufferedInputStream is = new BufferedInputStream(new FileInputStream(input));

			//parse
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			BuyLinksXmlParser mHandler = new BuyLinksXmlParser();
			xr.setContentHandler(mHandler);		   
			xr.parse(new InputSource(is));
			is.close();

			ArrayList<AffiliateLink> links = mHandler.getListLinks();
			ContentResolver resolver = mContext.getContentResolver();
			Uri uri = AlbumProvider.AffiliateLink.AffiliateLinks.CONTENT_URI;
			ContentValues values = new ContentValues();


			Cursor c=null;
			for(AffiliateLink link : links){
				try{
					String[] projection = { AlbumProvider.AffiliateLink.AffiliateLinks.AFF_ID };
					String where = AlbumProvider.AffiliateLink.AffiliateLinks.AFF_ALBUM_ID+" = ? AND "
							+ AlbumProvider.AffiliateLink.AffiliateLinks.AFF_SUPPLIER_NAME+" = ?";
					String[] selectionArgs = { ""+mAlbum.ID, link.supplierName };

					c = resolver.query(uri, projection, where, selectionArgs, null);
					if(c.moveToFirst()){
						log.write("buylink exists: "+link.toString());
					}else{
						values.clear();
						values.put(AlbumProvider.AffiliateLink.AffiliateLinks.AFF_ALBUM_ID, mAlbum.ID);
						values.put(AlbumProvider.AffiliateLink.AffiliateLinks.AFF_SUPPLIER_NAME, link.supplierName);
						values.put(AlbumProvider.AffiliateLink.AffiliateLinks.AFF_BUY_LINK, link.buyLink);
						values.put(AlbumProvider.AffiliateLink.AffiliateLinks.AFF_CURRENCY, link.currency);
						values.put(AlbumProvider.AffiliateLink.AffiliateLinks.AFF_AMOUNT, link.amount);
						values.put(AlbumProvider.AffiliateLink.AffiliateLinks.AFF_IS_PHYSICAL, link.isPhysicalMedia);
						values.put(AlbumProvider.AffiliateLink.AffiliateLinks.AFF_IS_SEARCH, link.isSearch);

						Uri resultUri = resolver.insert(uri, values);
						log.write("inserting buylink: "+link.toString());
					}
				}finally{
					if(c!=null) c.close();
					c = null;
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			log.close();
		}
	}

}
