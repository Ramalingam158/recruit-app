package com.recruitapp.apiservice.exception;

public class ResourceAlreadyExistsException extends CustomException {
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}
