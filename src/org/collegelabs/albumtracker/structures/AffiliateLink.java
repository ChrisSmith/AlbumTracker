package org.collegelabs.albumtracker.structures;

public class AffiliateLink {
	
	public String supplierName, buyLink, currency, amount;
	public boolean isSearch, isPhysicalMedia;
	
	public AffiliateLink(){
		supplierName = buyLink = currency = amount = "";
		isSearch = isPhysicalMedia = false;
	}
	
	@Override
	public String toString(){
		return new StringBuilder().append(supplierName)
			.append(", ").append(buyLink).append(", ").append(currency).append(", ")
			.append(amount).append(", ").append(isSearch).append(", ").append(isPhysicalMedia)
			.toString();
	}
	
}
