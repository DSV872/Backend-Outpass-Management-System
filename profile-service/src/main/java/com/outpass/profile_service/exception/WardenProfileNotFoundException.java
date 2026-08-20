package com.outpass.profile_service.exception;


public class WardenProfileNotFoundException extends RuntimeException {
	public WardenProfileNotFoundException(String message) {
		super(message);
	}
}