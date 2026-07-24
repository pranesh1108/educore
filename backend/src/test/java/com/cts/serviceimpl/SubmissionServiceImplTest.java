package com.cts.serviceimpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import com.cts.dto.GradeInputDTO;
import com.cts.dto.SubmissionOutputDTO;
import com.cts.entity.*;
import com.cts.mapper.StudentMapper;
import com.cts.repository.*;
import com.cts.util.SecurityUtils;

public class SubmissionServiceImplTest {

    @Mock private StudentMapper studentMapper;
    @Mock private SubmissionRepository submissionRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private InstructorRepository instructorRepository;

    @InjectMocks
    private SubmissionServiceImpl submissionService;

    private final String MOCK_EMAIL = "instructor@educore.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void gradeSubmission_success() {
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getLoggedInEmail).thenReturn(MOCK_EMAIL);

            Instructor instructor = Instructor.builder().instructorId(1L).build();
            Course course = Course.builder().courseId(10L).instructor(instructor).build();
            Submission submission = Submission.builder().submissionId(100L).course(course).build();

            GradeInputDTO gradeInput = GradeInputDTO.builder().grade(95.0).feedback("Excellent").build();
            SubmissionOutputDTO outputDTO = SubmissionOutputDTO.builder().submissionId(100L).grade(95.0).status("GRADED").build();

            when(instructorRepository.findByUser_Email(MOCK_EMAIL)).thenReturn(Optional.of(instructor));
            when(submissionRepository.findById(100L)).thenReturn(Optional.of(submission));
            when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
            when(submissionRepository.save(any(Submission.class))).thenReturn(submission);
            when(studentMapper.toSubmissionOutputDTO(submission)).thenReturn(outputDTO);

            SubmissionOutputDTO result = submissionService.gradeSubmission(100L, gradeInput);

            assertNotNull(result);
            assertEquals(95.0, result.getGrade());
            assertEquals("GRADED", result.getStatus());
        }
    }
}