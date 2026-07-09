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

import com.cts.dto.*;
import com.cts.service.RegistrarAcademicService;

public class RegistrarControllerTest {

    @Mock private RegistrarAcademicService academicService;

    @InjectMocks
    private RegistrarController registrarController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void provisionNewCourse_success_returns201() {
        RegistrarCourseCreateDTO input = RegistrarCourseCreateDTO.builder().title("Java FSE").instructorId(1L).build();
        RegistrarCourseResponseDTO output = RegistrarCourseResponseDTO.builder().courseId(1L).title("Java FSE").build();

        when(academicService.provisionNewCourse(any(RegistrarCourseCreateDTO.class))).thenReturn(output);

        ResponseEntity<RegistrarCourseResponseDTO> response = registrarController.provisionNewCourse(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Java FSE", response.getBody().getTitle());
    }

    @Test
    void filterByRole_success() {
        when(academicService.filterByRole(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Arrays.asList(StudentFilterOutputDTO.builder().name("Ravi").build()));

        ResponseEntity<Object> response = registrarController.filterByRole("student", null, null, null, null, null, null, "name", "asc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}