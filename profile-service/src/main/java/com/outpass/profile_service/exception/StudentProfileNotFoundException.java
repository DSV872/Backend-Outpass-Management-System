package com.outpass.profile_service.exception;

public class StudentProfileNotFoundException extends RuntimeException {
	public StudentProfileNotFoundException(String message) {
		super(message);
	}
}