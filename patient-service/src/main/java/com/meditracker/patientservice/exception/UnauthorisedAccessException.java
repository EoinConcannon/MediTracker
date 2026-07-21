package com.meditracker.patientservice.exception;

public class UnauthorisedAccessException extends RuntimeException {
	public UnauthorisedAccessException(String message) {
		super(message);
	}
}