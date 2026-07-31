package com.meditracker.appointmentservice.exception;

public class AppointmentConflictException extends RuntimeException {
	public AppointmentConflictException(String message) {
		super(message);
	}
}