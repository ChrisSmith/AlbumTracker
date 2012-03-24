package org.collegelabs.albumtracker.structures;

import java.util.ArrayList;
import java.util.Date;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class AlbumXmlParser extends DefaultHandler{

	// ===========================================================
	// Fields
	// ===========================================================

	//album fields
	private boolean in_album = false;
	private boolean in_album_name = false;
	private boolean in_album_mbid = false;
	private boolean in_album_url = false;
	private boolean in_album_img_small = false;
	private boolean in_album_img_med = false;
	private boolean in_album_img_large = false;
	private boolean in_album_img_xlarge = false;

	//artist fields	
	private boolean in_artist = false; 
	private boolean in_artist_name = false;
	private boolean in_artist_mbid = false;
	private boolean in_artist_url = false;
	
	private Album currentAlbum = null;
	private Artist currentArtist = null;
	
	
	private ArrayList<Album> listAlbums = null;
	
	public ArrayList<Album> getListAlbums(){ return listAlbums; }
	
	private boolean in_exception = false;
	private LastfmError exception = null;
	
	public LastfmError getException(){ return this.exception; }
	
	// ===========================================================
	// Methods
	// ===========================================================
	@Override
	public void startDocument() throws SAXException {
	}

	@Override
	public void endDocument() throws SAXException {
	}

	/** Gets be called on opening tags like: 
	 * <tag> 
	 * Can provide attribute(s), when xml was like:
	 * <tag attribute="attributeValue">*/
	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		swapElementStatus(localName, qName, atts, true);
	}
	
	/** Gets be called on closing tags like: 
	 * </tag> */
	@Override
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
		swapElementStatus(localName, qName, null, false);
	}
	
	/*
	 * Swap bools
	 */
	private void swapElementStatus(String localName, String qName, Attributes atts, boolean enabled){
		
		if(enabled && localName.equals("lfm")){
		
		}else if(localName.equals("error")){
			in_exception = enabled;
			if(enabled)	exception = new LastfmError(atts.getValue("code"));
		}else if (localName.equals("albums")) {//new album list
			if(enabled)	this.listAlbums=new ArrayList<Album>();
		}else if(localName.equals("album")){ //new album
			in_album=enabled;
			if(enabled){
				currentAlbum=new Album();
				String value = atts.getValue("releasedate");
				currentAlbum.release = new Date(value);
			}else{
				listAlbums.add(currentAlbum);
				currentAlbum = null;				
			}
		}else if(localName.equals("name")){
			if(this.in_artist)
				this.in_artist_name=enabled;
			else
				this.in_album_name=enabled;
		}else if(localName.equals("mbid")){
			if(this.in_artist)
				this.in_artist_mbid=enabled;
			else
				this.in_album_mbid=enabled;
		
		}else if(localName.equals("url")){
			if(this.in_artist)
				this.in_artist_url=enabled;
			else
				this.in_album_url=enabled;
			
		}else if(localName.equals("image")){
			if(enabled){
				String attrValue = atts.getValue("size");
				if(attrValue.equals("small")){
					this.in_album_img_small=true;
				}else if(attrValue.equals("medium")){
					this.in_album_img_med=true;
				}else if(attrValue.equals("large")){
					this.in_album_img_large=true;
				}else if(attrValue.equals("extralarge")){
					this.in_album_img_xlarge=true;
				}
			}else{
				this.in_album_img_small=false;
				this.in_album_img_med=false;
				this.in_album_img_large=false;
				this.in_album_img_xlarge=false;	
			}
		}else if(localName.equals("artist")){
			if(enabled){ 
				this.currentArtist = new Artist();
			}else{
				this.currentAlbum.artist = currentArtist;
			}
			this.in_artist=enabled;
		}
	}
	
	
	/** Gets be called on the following structure: 
	 * <tag>characters</tag> */
	@Override
    public void characters(char ch[], int start, int length) {
		String temp = new String(ch, start, length);
//		Log.d(SyncAdapter.TAG, "chars: " +temp);
		
		if(in_exception){
			if(!exception.wasMessageSetManually()) exception.setErrorMessage("");
			
			exception.setErrorMessage(exception.getErrorMessage()+temp);
		}
		
		if(in_album){
			if(in_artist){
				if(in_artist_name){
					currentArtist.name += temp;
				}else if(in_artist_mbid){
					currentArtist.mbid += temp;					
				}else if(in_artist_url){
					currentArtist.url += temp;
				}
			}else{
				if(in_album_name){
					currentAlbum.name += temp;
				}else if(in_album_mbid){
					currentAlbum.mbid += temp;
				}else if(in_album_url){
					currentAlbum.url += temp;					
				}else if(in_album_img_small){
					currentAlbum.img_small += temp;
				}else if(in_album_img_med){
					currentAlbum.img_medium += temp;
				}else if(in_album_img_large){
					currentAlbum.img_large += temp;
				}else if(in_album_img_xlarge){
					currentAlbum.img_xlarge += temp;
				}
			}
		}
    }
}
