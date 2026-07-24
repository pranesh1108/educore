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
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import com.cts.dto.*;
import com.cts.service.AssignmentService;
import com.cts.service.CourseService;
import com.cts.service.InstructorService;
import com.cts.service.SubmissionService;

public class InstructorControllerTest {

    @Mock private InstructorService instructorService;
    @Mock private CourseService courseService;
    @Mock private AssignmentService assignmentService;
    @Mock private SubmissionService submissionService;
    @Mock private Validator validator;

    @InjectMocks
    private InstructorController instructorController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAssignedCourses_success() {
        CourseOutputDTO course = CourseOutputDTO.builder().courseId(1L).title("Spring Boot").build();
        when(courseService.getAssignedCourses()).thenReturn(List.of(course));

        ResponseEntity<List<CourseOutputDTO>> response = instructorController.getAssignedCourses();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Spring Boot", response.getBody().get(0).getTitle());
    }

    @Test
    void gradeSubmission_success() {
        GradeInputDTO input = GradeInputDTO.builder().grade(90.0).feedback("Great job").build();
        SubmissionOutputDTO output = SubmissionOutputDTO.builder().submissionId(1L).grade(90.0).build();

        when(submissionService.gradeSubmission(eq(1L), any(GradeInputDTO.class))).thenReturn(output);

        ResponseEntity<SubmissionOutputDTO> response = instructorController.gradeSubmission(1L, input);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(90.0, response.getBody().getGrade());
    }
}