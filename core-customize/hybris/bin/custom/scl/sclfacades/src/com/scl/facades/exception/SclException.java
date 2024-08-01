package com.scl.facades.exception;

public class SclException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public SclException() {
		super();
	}
	public SclException(String message) {
		super(message);
	}
	
    public SclException(String message, Throwable cause) {
        super(message, cause);
    }
}
