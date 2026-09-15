package com.internship.registration.exception;

import org.springframework.http.HttpStatus;

public class RegistrationException extends RuntimeException {

    private final int code;
    private final HttpStatus status;

    public RegistrationException(int code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}