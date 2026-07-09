package com.cts.exception;

public class InstructorNotFoundException extends RuntimeException {
    public InstructorNotFoundException(String message) {
        super(message);
    }
}