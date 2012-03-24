package org.collegelabs.albumtracker.structures;

public class LastfmError extends Exception{
	private static final long serialVersionUID = 5027600660871576804L;
	
	public String message = "Unknown Exception";
	private int pCode = -1;
	
	
	public LastfmError(String code){
		
		try{
			pCode = Integer.parseInt(code);				
		}catch(NumberFormatException e){
			pCode = -1;
		}
		
		switch(pCode){
		case 2:
			message = "Invalid service - This service does not exist";
			break;
		case 3:
			message = "Invalid Method - No method with that name in this package";
			break;
		case 4:
			message = "Authentication Failed - You do not have permissions to access the service";
			break;
		case 5:
			message = "Invalid format - This service doesn't exist in that format";
			break;
		case 6:
			message = "Invalid parameters - Your request is missing a required parameter";
			break;
		case 7:
			message = "Invalid resource specified";
			break;
		case 8:
			message = "Operation failed - Something else went wrong";
			break;
		case 9:
			message = "Invalid session key - Please re-authenticate";
			break;
		case 10:
			message = "Invalid API key - You must be granted a valid key by last.fm";
			break;
		case 11:
			message = "Service Offline - This service is temporarily offline. Try again later.";
			break;
		case 13:
			message = "Invalid method signature supplied";
			break;
		case 16:
			message = "There was a temporary error processing your request. Please try again";
			break;
		case 26:
			message = "Suspended API key - Access for your account has been suspended, please contact Last.fm";
			break;
		case 29:
			message = "Rate limit exceeded - Your IP has made too many requests in a short period";
			break;
			
		default:
			break;
		}
	}	
	
	public int getErrorCode(){ return pCode; }
	public String getErrorMessage(){ return message; }
	
	public void setErrorMessage(String msg){
		messageSetManually = true;
		message = msg;
	}
	
	private boolean messageSetManually = false;

	public boolean wasMessageSetManually(){ return messageSetManually; }
}
