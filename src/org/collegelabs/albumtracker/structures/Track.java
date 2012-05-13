package org.collegelabs.albumtracker.structures;

public class Track {

	private int mRank, mLength;
	private String mName, mUrl;
	private Artist mArtist;
	
	public Track(){
		mLength = mRank = 0;
		mName = mUrl = "";
		mArtist = null;
	}

	public String getFormattedLength(){
		int minutes = mLength / 60;
		int seconds = mLength % 60;
		
		return String.format("%d:%02d", minutes, seconds);
	}
	
	public int getRank() {
		return mRank;
	}

	public void setRank(int mRank) {
		this.mRank = mRank;
	}

	public int getLength() {
		return mLength;
	}

	public void setLength(int mLength) {
		this.mLength = mLength;
	}

	public String getName() {
		return mName;
	}

	public void setName(String mName) {
		this.mName = mName;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String mUrl) {
		this.mUrl = mUrl;
	}

	public Artist getArtist() {
		return mArtist;
	}

	public void setArtist(Artist mArtist) {
		this.mArtist = mArtist;
	}
	
	
}
