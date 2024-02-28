package com.eydms.facades.exception;

public class EyDmsException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public EyDmsException() {
		super();
	}
	public EyDmsException(String message) {
		super(message);
	}
	
    public EyDmsException(String message, Throwable cause) {
        super(message, cause);
    }
}
