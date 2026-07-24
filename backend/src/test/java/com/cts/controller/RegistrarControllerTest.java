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
import com.cts.repository.AuditLogRepository;
import com.cts.service.RegistrarAcademicService;

public class RegistrarControllerTest {

    @Mock private AuditLogRepository auditLogRepository;
    @Mock private RegistrarAcademicService academicService;

    @InjectMocks
    private RegistrarController registrarController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void provisionNewCourse_success() {
        RegistrarCourseCreateDTO input = RegistrarCourseCreateDTO.builder().title("Python").instructorId(1L).build();
        RegistrarCourseResponseDTO output = RegistrarCourseResponseDTO.builder().courseId(1L).title("Python").build();

        when(academicService.provisionNewCourse(any(RegistrarCourseCreateDTO.class))).thenReturn(output);

        ResponseEntity<RegistrarCourseResponseDTO> response = registrarController.provisionNewCourse(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Python", response.getBody().getTitle());
    }

    @Test
    void filterByRole_student_success() {
        when(academicService.filterByRole(anyString(), any(), any(), any(), any(), any(), any(), anyString(), anyString()))
                .thenReturn(List.of(StudentFilterOutputDTO.builder().name("Ravi").build()));

        ResponseEntity<Object> response = registrarController.filterByRole(
                "student", "Ravi", null, null, null, null, null, "name", "asc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}