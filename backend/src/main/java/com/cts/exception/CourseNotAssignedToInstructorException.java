package com.cts.exception;

public class CourseNotAssignedToInstructorException extends RuntimeException {
    public CourseNotAssignedToInstructorException(String message) {
        super(message);
    }
}