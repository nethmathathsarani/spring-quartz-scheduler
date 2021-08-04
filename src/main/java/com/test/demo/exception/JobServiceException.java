package com.test.demo.exception;

import org.springframework.http.HttpStatus;

public class JobServiceException extends Exception {

	//private final JobExceptionTypeEnum jobExceptionTypeEnum;

	private final String errorText;
	private final HttpStatus httpStatusCode;


	public JobServiceException(String errorText, HttpStatus httpStatusCode) {
		super(errorText);
		this.errorText = errorText;
		this.httpStatusCode = httpStatusCode;
	}
}