package org.collegelabs.albumtracker.structures;

public class ExceptionWrapper<T> {

	private T mData;
	private Exception mException;
	
	
	public ExceptionWrapper(T data){
		if(data == null) throw new NullPointerException("Data can't be null");
		
		mData = data;
		mException = null;
	}
	
	public ExceptionWrapper(Exception exception){
		if(exception == null) throw new NullPointerException("Exception can't be null");
		
		mData = null;
		mException = exception;
	}
	
	public T getData(){
		return mData;
	}
	
	public Exception getException(){
		return mException;
	}
	
	public boolean hasException(){
		return mException != null;
	}
}
