package com.cts.serviceimpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import com.cts.dto.*;
import com.cts.entity.*;
import com.cts.mapper.AssignmentMapper;
import com.cts.repository.*;
import com.cts.service.FileStorageService;
import com.cts.util.SecurityUtils;

public class AssignmentServiceImplTest {

    @Mock private AssignmentMapper assignmentMapper;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private AssignmentFileRepository assignmentFileRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private InstructorRepository instructorRepository;
    @Mock private FileStorageService fileStorageService;
    @Mock private ExamResultRepository examResultRepository;
    @Mock private CourseEnrollmentRepository enrollmentRepository;

    @InjectMocks
    private AssignmentServiceImpl assignmentService;

    private final String MOCK_EMAIL = "instructor@educore.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void publishAssignment_withoutFile_success() {
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getLoggedInEmail).thenReturn(MOCK_EMAIL);

            Instructor mockInstructor = Instructor.builder().instructorId(1L).build();
            Course mockCourse = Course.builder().courseId(10L).title("Java").build();
            Assignment mockAssignment = Assignment.builder().assignmentId(100L).build();
            AssignmentInputDTO inputDTO = AssignmentInputDTO.builder().courseId(10L).title("Assignment 1").totalMarks(100.0).dueDate(LocalDateTime.now().plusDays(2)).build();

            when(instructorRepository.findByUser_Email(MOCK_EMAIL)).thenReturn(Optional.of(mockInstructor));
            when(enrollmentRepository.findByCourse_CourseId(10L)).thenReturn(Collections.emptyList());
            when(instructorRepository.existsById(1L)).thenReturn(true);
            when(courseRepository.findByCourseIdAndInstructor_InstructorId(10L, 1L)).thenReturn(Optional.of(mockCourse));
            when(assignmentRepository.save(any(Assignment.class))).thenReturn(mockAssignment);
            when(assignmentMapper.toAssignmentOutputDTO(eq(mockAssignment), anyList())).thenReturn(new AssignmentOutputDTO());

            AssignmentOutputDTO result = assignmentService.publishAssignment(inputDTO, null);

            assertNotNull(result);
            verify(assignmentRepository).save(any(Assignment.class));
        }
    }
}