package com.outpass.profile_service.exception;

public class SecurityProfileNotFoundException extends RuntimeException {
	public SecurityProfileNotFoundException(String message) {
		super(message);
	}
}