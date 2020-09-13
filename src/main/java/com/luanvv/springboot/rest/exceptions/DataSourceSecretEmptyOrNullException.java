package com.luanvv.springboot.rest.exceptions;

public class DataSourceSecretEmptyOrNullException extends DataSourceSecretInvalidException {

	public DataSourceSecretEmptyOrNullException(String message) {
		super(message);
	}

}
