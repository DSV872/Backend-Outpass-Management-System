package com.outpass.outpass_service.exception;

public class WardenDutyException extends RuntimeException {

    public WardenDutyException(String message) {
        super(message);
    }

    public WardenDutyException(String message, Throwable cause) {
        super(message, cause);
    }
}