package org.collegelabs.albumtracker.structures;

import org.collegelabs.albumtracker.content.AlbumProvider;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;


public class Artist implements Parcelable{

	public String name="";
	public String mbid="";
	public String url = "";
	
	public Artist() {
		name = mbid = url = "";
	}

	/**
	 * 
	 * @param name
	 * @param mbid
	 * @param url
	 */
	public Artist(String name, String mbid, String url) {
		this.name = name;
		this.mbid = mbid;
		this.url = url;
	}
	
	public Artist(Cursor c) {
		final int colArtistName = c.getColumnIndexOrThrow(AlbumProvider.Album.Albums.ARTIST_NAME);
		final int colMBid = c.getColumnIndexOrThrow(AlbumProvider.Album.Albums.ARTIST_MBID);
		
		this.name = c.getString(colArtistName);
		this.mbid = c.getString(colMBid);
		this.url = "";		
	}
	
	@Override
	public String toString(){
		return new StringBuilder().append(name)
		.append("-").append(mbid)
		.append("-").append(url).toString();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	
	/*
	 * Inflate after serialization
	 */
	public Artist(Parcel in) {
		name = in.readString();
		mbid = in.readString();
		url = in.readString();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(mbid);
		dest.writeString(url);
	}
	
	public static final Parcelable.Creator<Artist> CREATOR
		= new Parcelable.Creator<Artist>() {
		public Artist createFromParcel(Parcel in) {
			return new Artist(in);
		}

		public Artist[] newArray(int size) {
			return new Artist[size];
		}
	};
}
