package com.luanvv.springboot.rest.exceptions;

public class DataSourceSecretInvalidException extends RuntimeException {

	public DataSourceSecretInvalidException() {
		super();
	}
	
	public DataSourceSecretInvalidException(String message) {
		super(message);
	}

	public DataSourceSecretInvalidException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
