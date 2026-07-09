package com.cts.exception;

@SuppressWarnings("serial")
public class NoDetailsAvailableException extends RuntimeException {

    public NoDetailsAvailableException(String message) {
        super(message);
    }
}
