package com.cts.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import com.cts.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //@Valid failures on Input DTO
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    //Authorization Exception
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(
            com.cts.exception.AccessDeniedException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN); // 403
    }
     
    
    // User
    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<String> handleInvalidEmailException(InvalidEmailException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NoDetailsAvailableException.class)
    public ResponseEntity<String> handleNoDetailsAvailableException(NoDetailsAvailableException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    //Instructor
    @ExceptionHandler(InstructorNotFoundException.class)
    public ResponseEntity<String> handleInstructorNotFoundException(InstructorNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CourseNotAssignedToInstructorException.class)
    public ResponseEntity<String> handleCourseNotAssignedToInstructorException(
            CourseNotAssignedToInstructorException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    //Student
    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<String> handleStudentNotFoundException(StudentNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    //Course
    @ExceptionHandler(CourseNotFoundException.class)
    public ResponseEntity<String> handleCourseNotFoundException(CourseNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CourseAlreadyExistsException.class)
    public ResponseEntity<String> handleCourseAlreadyExistsException(CourseAlreadyExistsException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    //Assignment
    @ExceptionHandler(AssignmentNotFoundException.class)
    public ResponseEntity<String> handleAssignmentNotFoundException(AssignmentNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    //Submission
    @ExceptionHandler(SubmissionNotFoundException.class)
    public ResponseEntity<String> handleSubmissionNotFoundException(SubmissionNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidSubmissionException.class)
    public ResponseEntity<String> handleInvalidSubmissionException(InvalidSubmissionException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidGradeException.class)
    public ResponseEntity<String> handleInvalidGradeException(InvalidGradeException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    //File
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<String> handleFileStorageException(FileStorageException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<String> handleInvalidFileException(InvalidFileException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    //Registrar
    @ExceptionHandler(AcademicException.class)
    public ResponseEntity<String> handleAcademicException(AcademicException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ProfileAlreadyExistsException.class)
    public ResponseEntity<String> handleProfileAlreadyExistsException(ProfileAlreadyExistsException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    //Exam
    @ExceptionHandler(ExamNotFoundException.class)
    public ResponseEntity<String> handleExamNotFoundException(ExamNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    //Business
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> handleBusinessException(BusinessException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // Database constraint violations
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDatabaseConstraints(DataIntegrityViolationException ex) {
        return new ResponseEntity<>(
                "Database constraint violation: This entry or relationship already exists.",
                HttpStatus.CONFLICT);
    }

//    //Enrollment
//    @ExceptionHandler(EnrollmentException.class)
//    public ResponseEntity<String> handleEnrollmentException(EnrollmentException ex) {
//        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
//    }

    @ExceptionHandler(NotEnrolledException.class)
    public ResponseEntity<String> handleNotEnrolledException(NotEnrolledException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
    }


    //ConstraintViolation
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolationException(
            ConstraintViolationException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    //Missing required @RequestParam
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingParams(
            MissingServletRequestParameterException ex) {
        return new ResponseEntity<>(
                "Required parameter '" + ex.getParameterName() + "' is missing.",
                HttpStatus.BAD_REQUEST);
    }

    //Wrong type for path variable or request param
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        return new ResponseEntity<>(
                "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName()
                + "'. Expected type: " + ex.getRequiredType().getSimpleName(),
                HttpStatus.BAD_REQUEST);
    }

    // Malformed JSON / bad date format
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex) {
        String message = "Malformed request body. Please check field types and date formats.";
        if (ex.getMessage() != null && ex.getMessage().contains("LocalDateTime")) {
            message = "Invalid date/time format. Use 'yyyy-MM-dd HH:mm' (e.g. 2026-08-15 20:00).";
        }
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    //Resource not found (generic)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    //Generic fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        ex.printStackTrace();
        return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex) {
        return new ResponseEntity<>(
                Map.of(
                        "status", 413,
                        "error", "File Too Large",
                        "message", "File size exceeds the maximum allowed limit of 10MB. Please upload a smaller file."
                ),
                HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler({InvalidCredentialsException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponseDTO> handleAuthenticationFailure(
            Exception ex, HttpServletRequest request) {

        ErrorResponseDTO errorDTO = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value()) // Returns a 401 Unauthorized
                .error("Unauthorized Access")
                .message("Invalid email or password combination. Please check your credentials.")
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorDTO, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(EnrollmentException.class)
    public ResponseEntity<ErrorResponseDTO> handleEnrollmentException(
            EnrollmentException ex,
            org.springframework.web.context.request.WebRequest request) {

        ErrorResponseDTO errorDTO = ErrorResponseDTO.builder()
                .timestamp(java.time.LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value()) // 409 Conflict
                .error("Enrollment Error")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorDTO, HttpStatus.CONFLICT);
    }


}