package com.cts.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void updateStudentProfile_success_returns200() {
        StudentInputDTO input = StudentInputDTO.builder().userId(1L).educationLevel("BACHELOR").build();
        StudentOutputDTO output = StudentOutputDTO.builder().studentId(1L).educationLevel("BACHELOR").build();

        when(studentService.updateStudentProfile(any(StudentInputDTO.class))).thenReturn(output);

        ResponseEntity<StudentOutputDTO> response = studentController.updateStudentProfile(input);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("BACHELOR", response.getBody().getEducationLevel());
    }
}