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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.cts.dto.RegistrarCourseResponseDTO;
import com.cts.service.StudentService;

public class CourseControllerTest {

    @Mock
    private StudentService studentService;

    @InjectMocks
    private CourseController courseController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllCourses_success() {
        RegistrarCourseResponseDTO course = RegistrarCourseResponseDTO.builder().courseId(10L).title("Java FSE").build();
        Page<RegistrarCourseResponseDTO> page = new PageImpl<>(List.of(course));

        when(studentService.filterCourses(any(), any(), any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<RegistrarCourseResponseDTO>> response = courseController.getAllCourses("Java", null, 0, 10, "title,asc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("Java FSE", response.getBody().getContent().get(0).getTitle());
    }

    @Test
    void getSyllabusInline_success() {
        Resource mockResource = new ByteArrayResource("PDF".getBytes()) {
            @Override
            public String getFilename() {
                return "syllabus.pdf";
            }
        };

        when(studentService.getSyllabusResource(10L)).thenReturn(mockResource);

        ResponseEntity<Resource> response = courseController.getSyllabusInline(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}