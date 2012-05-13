package org.collegelabs.albumtracker.structures;

import org.xml.sax.helpers.DefaultHandler;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class TrackParser extends DefaultHandler {
	
	public ArrayList<Track> getTracks(){ return mTracks; }
	public LastfmError getException(){ return this.exception; }
	
	
	private ArrayList<Track> mTracks = null;
	private LastfmError exception = null;
	private boolean inArtist = false;
	private boolean inTrack = false;
	
	
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
	
	
	Track currentTrack = null;
	Artist currentArtist = null;
	
	/*
	 * Swap booleans
	 */
	private void swapElementStatus(String localName, String qName, Attributes atts, boolean openTag){
		
		if(openTag && localName.equals("lfm")){
		
		}else if(openTag && localName.equals("error")){
			this.exception = new LastfmError(atts.getValue("code"));
		}else if(localName.equals("artist")){
			inArtist = openTag;
			if(inArtist){
				currentArtist = new Artist();
			}else if(inTrack){
				currentTrack.setArtist(currentArtist);
				currentArtist = null;
			}
		}else if (openTag && localName.equals("tracks")) {
			mTracks = new ArrayList<Track>();
		}else if(localName.equals("track")){
			inTrack = openTag;
			if(openTag){
				currentTrack = new Track();
			}else{
				mTracks.add(currentTrack);
				currentTrack = null;				
			}
		}else if(!openTag){ 
			temp = temp.trim();
			
			if(inArtist){
				if(localName.equals("name")){
					currentArtist.name = temp;
				}else if(localName.equals("mbid")){
					currentArtist.mbid = temp;
				}else if(localName.equals("url")){
					currentArtist.url = temp;
				}
				
			}else if(inTrack){
				if(localName.equals("name")){
					currentTrack.setName(temp);
				}else if(localName.equals("duration")){
					try{
						int durration = Integer.parseInt(temp);
						currentTrack.setLength(durration);
					}catch(NumberFormatException e){}
				}else if(localName.equals("url")){
					currentTrack.setUrl(temp);
				}
			}
			
			temp = "";
		}
	}
	
	
	/** Gets be called on the following structure: 
	 * <tag>characters</tag> */
	String temp  = "";
	@Override
    public void characters(char ch[], int start, int length) {
		temp += new String(ch, start, length);
    }
}
