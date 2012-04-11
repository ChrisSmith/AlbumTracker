package org.collegelabs.albumtracker.structures;

import java.util.Date;

import org.collegelabs.albumtracker.content.AlbumProvider;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

/*
 * 	-Name
	-release
	-mbid
	-url
	-image-small (34x34)
	-image-medium (64x64)
	-image-large (126x126)
	-image-extralarge (300x300)
 */

public class Album implements Parcelable {
	public int ID = -1; //> 0 when inserted into database
	public String name;
	public Date release;
	public String mbid;
	public String url;
	public String img_small;
	public String img_medium;
	public String img_large;
	public String img_xlarge;
	
	public Artist artist;
	public boolean isNew = false;
	public boolean isStarred = false;
	
	public Album(){
		name = mbid = url = img_small = img_medium = img_large = img_xlarge = "";
		release = new Date(0);
	}
	
	public Album(Cursor c){
		final int colAlbumName = c.getColumnIndexOrThrow(AlbumProvider.Album.Albums.ALBUM_NAME);
		final int colRelease = c.getColumnIndexOrThrow(AlbumProvider.Album.Albums.ALBUM_RELEASE_DATE);
		final int colImg = c.getColumnIndexOrThrow(AlbumProvider.Album.Albums.ALBUM_IMG_XLARGE);
		final int colID = c.getColumnIndexOrThrow(AlbumProvider.Album.Albums.ALBUM_ID);
		final int colStarred = c.getColumnIndexOrThrow(AlbumProvider.Album.Albums.ALBUM_STARRED);
		final int colNew = c.getColumnIndexOrThrow(AlbumProvider.Album.Albums.ALBUM_NEW);
		
		this.ID = c.getInt(colID);
		this.name = c.getString(colAlbumName);
		this.release = new Date(c.getLong(colRelease));
		this.img_xlarge = c.getString(colImg);
		this.isNew = c.getInt(colNew) == 1;
		this.isStarred = c.getInt(colStarred) == 1;
	}
	
	@Override
	public String toString(){
		return new StringBuilder()
		.append(ID).append("-")
		.append(name).append("-")
		.append(release).append("-")
		.append(mbid).append("-")
		.append(url).append("-")
		.append(img_small).append("-")
		.append(img_medium).append("-")
		.append(img_large).append("-")
		.append(img_xlarge).append("-")
		.append(artist).toString();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	
	/*
	 * Inflate after serialization
	 */
	public Album(Parcel in) {
		ID = in.readInt();
		name = in.readString();
		release = new Date(in.readLong());
		mbid = in.readString();
		url = in.readString();
		img_small = in.readString();
		img_medium = in.readString();
		img_large = in.readString();
		img_xlarge = in.readString();
		isNew = (in.readInt() == 1) ? true : false;
		isStarred = (in.readInt() == 1) ? true : false;
		artist = in.readParcelable(this.getClass().getClassLoader());
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(ID);
		dest.writeString(name);
		dest.writeLong(release.getTime());
		dest.writeString(mbid);
		dest.writeString(url);
		dest.writeString(img_small);
		dest.writeString(img_medium);
		dest.writeString(img_large);
		dest.writeString(img_xlarge);
		dest.writeInt(isNew ? 1 : 0);
		dest.writeInt(isStarred ? 1 : 0);
		dest.writeParcelable(artist, flags);
	}
	
	public static final Parcelable.Creator<Album> CREATOR
		= new Parcelable.Creator<Album>() {
		public Album createFromParcel(Parcel in) {
			return new Album(in);
		}

		public Album[] newArray(int size) {
			return new Album[size];
		}
	};
}
