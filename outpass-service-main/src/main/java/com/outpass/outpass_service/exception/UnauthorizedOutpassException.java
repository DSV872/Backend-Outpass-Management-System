package com.outpass.outpass_service.exception;

public class UnauthorizedOutpassException extends RuntimeException {

    public UnauthorizedOutpassException(String message) {
        super(message);
    }
}