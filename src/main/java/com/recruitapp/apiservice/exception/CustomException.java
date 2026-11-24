package com.recruitapp.apiservice.exception;

public class CustomException extends RuntimeException {
    public CustomException(String message) {
        super(message);
    }
}
