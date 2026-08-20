package com.outpass.profile_service.exception;

public class ProfileBusinessException extends RuntimeException {
	public ProfileBusinessException(String message) {
		super(message);
	}

	public ProfileBusinessException(String message, Throwable cause) {
		super(message, cause);
	}
}