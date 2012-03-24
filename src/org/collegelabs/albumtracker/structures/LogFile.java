package org.collegelabs.albumtracker.structures;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.collegelabs.albumtracker.BuildConfig;
import org.collegelabs.albumtracker.Constants;

import android.content.Context;
import android.util.Log;

public class LogFile{

	/*
	 * Instance
	 */
	private static final String TAG = Constants.TAG;
	
	private BufferedOutputStream os;
	private boolean canWrite = false;
	
	public LogFile(Context c){
		try{
			//only write to this file in debug mode. This is useful in combination
			//with our background syncadapter because the Logcat is too short lived.
			//These file writes will persist to disk where we can retrieve them at our convenience.
			if(BuildConfig.DEBUG){
				open(c);
				canWrite = true;				
			}
		}catch(IOException e){
			//unable to open log file. All calls to write will be ignored (including logcat outputs)
			canWrite = false;
		}
	}
	
	private void open(Context c) throws IOException{
		File t = c.getExternalCacheDir();

		if(t == null) throw new IOException("unable to open external cache dir. sdcard might not be mounted/writable");
		
		if(!t.exists())
			t.mkdirs();

		File f = new File(t,TAG+"_log.txt");
		try{
			if(!f.exists()){
				f.createNewFile();
				if(BuildConfig.DEBUG) Log.i(TAG,"created log file");
			}else{
				if(BuildConfig.DEBUG) Log.i(TAG,"log file exists");
			}
		}catch(Exception e){
			if(BuildConfig.DEBUG) Log.e(TAG,"unable to create log file");
		}
		os = new BufferedOutputStream(new FileOutputStream(f,true));
	}

	public void close(){
		canWrite = false;
		
		if(os!=null){
			try {
				os.flush();
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void write(final String msg){
		if(!canWrite) return;
		
		if(BuildConfig.DEBUG) Log.d(TAG,msg);
		
		Date d = new Date();
		try {
			os.write(("["+d.getHours()+":"+d.getMinutes()+":"+d.getSeconds()+"] "+msg+"\n")
					.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void write(final Throwable e){
		if(!canWrite) return;
		
		if(BuildConfig.DEBUG) Log.e(TAG,e.toString());
		
		e.printStackTrace();
		getCauses(e);	
	}
	
	private void getCauses(Throwable cause){
		if(cause != null) {
			write(cause.toString());
			StackTraceElement[] arr = cause.getStackTrace();
			for (int i=0; i<arr.length; i++){
				write(arr[i].toString());
			}
			getCauses(cause.getCause()); //recur
		}
	}
}


