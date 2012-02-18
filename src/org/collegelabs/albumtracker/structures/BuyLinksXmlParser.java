package org.collegelabs.albumtracker.structures;

import java.util.ArrayList;

import org.collegelabs.albumtracker.activities.MainActivity;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class BuyLinksXmlParser extends DefaultHandler {
	
	private boolean isPhysicalMedia = false;
	
	private ArrayList<AffiliateLink> listLinks = null;
	public ArrayList<AffiliateLink> getListLinks(){ return listLinks; }
	
	private LastfmError exception = null;
	public LastfmError getException(){ return this.exception; }
	
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
	
	
	AffiliateLink currentLink = null;
	/*
	 * Swap booleans
	 */
	private void swapElementStatus(String localName, String qName, Attributes atts, boolean openTag){
		
		if(openTag && localName.equals("lfm")){
		
		}else if(openTag && localName.equals("error")){
			this.exception = new LastfmError(atts.getValue("code"));
		}else if (openTag && localName.equals("affiliations")) {
			listLinks = new ArrayList<AffiliateLink>();
		}else if(localName.equals("affiliation")){ 
			if(openTag){
				currentLink=new AffiliateLink();
				currentLink.isPhysicalMedia = isPhysicalMedia;
			}else{
				listLinks.add(currentLink);
				currentLink = null;				
			}
		}else if(localName.equals("physicals")){
			isPhysicalMedia = openTag;
		}else if(!openTag){ 
			temp = temp.trim();
			if(localName.equals("supplierName")){
				currentLink.supplierName = temp;
			}else if(localName.equals("currency")){
				currentLink.currency = temp;
			}else if(localName.equals("amount")){
				currentLink.amount = temp;
			}else if(localName.equals("buyLink")){
				currentLink.buyLink = temp;
			}else if(localName.equals("isSearch")){
				currentLink.isSearch = Integer.parseInt(temp) == 1;
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
