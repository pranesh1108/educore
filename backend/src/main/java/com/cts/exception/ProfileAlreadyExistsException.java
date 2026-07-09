package com.cts.exception;
public class ProfileAlreadyExistsException extends RuntimeException {
    public ProfileAlreadyExistsException(String message) { super(message); }
}