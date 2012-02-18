package org.collegelabs.albumtracker;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.collegelabs.albumtracker.structures.Album;

import android.content.Context;

public abstract class LastfmHelper {

	public static String LASTFM = "http://ws.audioscrobbler.com/2.0/?method=";
    
	public static String GetNewReleases(final Context context, String username) throws UnsupportedEncodingException{
		return new StringBuilder(LASTFM).append("user.getnewreleases")
			.append("&api_key=").append(Utils.getApiKey(context, Utils.LAST_FM_KEY))
			.append("&user=").append(URLEncoder.encode(username, "UTF-8"))
			.toString();
	}
	
	//append &artist= &album= or &mbid=
    public static String getBuyLinks(final Context context, Album album) throws UnsupportedEncodingException{
		StringBuilder sb = new StringBuilder(LASTFM).append("album.getbuylinks")
			.append("&api_key=").append(Utils.getApiKey(context, Utils.LAST_FM_KEY))
			.append("&country=").append(URLEncoder.encode("United States", "UTF-8"));
		
		if(album.mbid != null && album.mbid.length() > 0){
			sb.append("&mbid=").append(URLEncoder.encode(album.mbid, "UTF-8"));
		}else{
			sb.append("&artist=").append(URLEncoder.encode(album.artist.name, "UTF-8"))
			.append("&album=").append(URLEncoder.encode(album.name, "UTF-8"));
		}
		
		return sb.toString();
	}
	
}
