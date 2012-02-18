package org.collegelabs.albumtracker.structures;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.collegelabs.albumtracker.Constants;

import android.content.Context;
import android.util.Log;

public class LogFile{

	/*
	 * Instance
	 */
	private static final String TAG = Constants.TAG;
	private static final boolean DEBUG = Constants.DEBUG;
	
	private BufferedOutputStream os;

	public LogFile(Context c){
		super();
		open(c);
	}
	
	private void open(Context c){
		try{
			File t = c.getExternalCacheDir();

			if(!t.exists())
				t.mkdirs();

			File f = new File(t,TAG+"_log.txt");
			try{
				if(!f.exists()){
					f.createNewFile();
					Log.i(TAG,"created log file");
				}else{
					Log.i(TAG,"log file exists");
				}
			}catch(Exception e){
				Log.e(TAG,"unable to create log file");
			}
			os = new BufferedOutputStream(new FileOutputStream(f,true));
		}catch(Exception e){
			Log.e(TAG,e.toString());
			e.printStackTrace();
		}
	}

	public void close(){
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
		if(!DEBUG){
			return;
		}
		
		Log.d(TAG,msg);
		
		Date d = new Date();
		try {
			os.write(("["+d.getHours()+":"+d.getMinutes()+":"+d.getSeconds()+"] "+msg+"\n")
					.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void write(final Throwable e){
		if(!DEBUG) return;
		Log.e(TAG,e.toString());
		
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


