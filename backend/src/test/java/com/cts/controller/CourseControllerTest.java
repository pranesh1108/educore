package com.cts.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.cts.dto.RegistrarCourseResponseDTO;
import com.cts.service.RegistrarAcademicService;
import com.cts.service.StudentService;

public class CourseControllerTest {

    @Mock private RegistrarAcademicService academicService;
    @Mock private StudentService studentService;

    @InjectMocks
    private CourseController courseController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllCourses_noFilters_success() {
        RegistrarCourseResponseDTO course = RegistrarCourseResponseDTO.builder().courseId(10L).title("Java FSE").build();
        when(academicService.getAllConfiguredCourses()).thenReturn(Arrays.asList(course));

        ResponseEntity<List<RegistrarCourseResponseDTO>> response = courseController.getAllCourses(null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Java FSE", response.getBody().get(0).getTitle());
    }
}