package com.cts.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.cts.dto.*;
import com.cts.service.StudentService;

public class StudentControllerTest {

    @Mock private StudentService studentService;

    @InjectMocks
    private StudentController studentController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getStudentProfile_success() {
        StudentOutputDTO output = StudentOutputDTO.builder().studentId(1L).fieldOfInterest("JAVA").build();
        when(studentService.getStudentProfile()).thenReturn(output);

        ResponseEntity<StudentOutputDTO> response = studentController.getStudentProfile();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("JAVA", response.getBody().getFieldOfInterest());
    }

    @Test
    void enrollInCourse_success() {
        EnrollmentOutputDTO dto = EnrollmentOutputDTO.builder().enrollmentId(1L).courseTitle("Java").build();
        when(studentService.enrollInCourse(10L)).thenReturn(dto);

        ResponseEntity<EnrollmentOutputDTO> response = studentController.enrollInCourse(10L);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Java", response.getBody().getCourseTitle());
    }
}