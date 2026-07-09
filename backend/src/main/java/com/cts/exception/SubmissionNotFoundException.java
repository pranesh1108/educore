package com.cts.exception;

public class SubmissionNotFoundException extends RuntimeException {
    public SubmissionNotFoundException(String message) {
        super(message);
    }
}