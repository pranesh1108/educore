package com.cts.exception;

@SuppressWarnings("serial")
public class InvalidEmailException extends RuntimeException {

    public InvalidEmailException(String message) {
        super(message);
    } 
}