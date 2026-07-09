package com.cts.exception;

public class InvalidSubmissionException extends RuntimeException {
    public InvalidSubmissionException(String message) {
        super(message);
    }
}