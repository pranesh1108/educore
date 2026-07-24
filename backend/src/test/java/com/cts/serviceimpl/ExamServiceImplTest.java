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

import com.cts.dto.ExamInputDTO;
import com.cts.dto.ExamOutputDTO;
import com.cts.entity.*;
import com.cts.enumerate.ExamStatus;
import com.cts.mapper.ExamMapper;
import com.cts.repository.*;
import com.cts.util.SecurityUtils;

public class ExamServiceImplTest {

    @Mock private ExamMapper examMapper;
    @Mock private ExamRepository examRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private InstructorRepository instructorRepository;
    @Mock private ExamCoordinatorRepository examCoordinatorRepository;

    @InjectMocks
    private ExamServiceImpl examService;

    private final String MOCK_EMAIL = "coordinator@educore.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createExam_success() {
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getLoggedInEmail).thenReturn(MOCK_EMAIL);

            ExamCoordinator coordinator = ExamCoordinator.builder().coordinatorId(1L).build();            Course course = Course.builder().courseId(10L).title("Java").build();
            Instructor instructor = Instructor.builder().instructorId(2L).build();
            Exam exam = Exam.builder().examId(1L).title("Java Exam").status(ExamStatus.ACTIVE).build();
            ExamOutputDTO outputDTO = ExamOutputDTO.builder().examId(1L).title("Java Exam").build();

            ExamInputDTO inputDTO = ExamInputDTO.builder()
                    .courseId(10L)
                    .instructorId(2L)
                    .title("Java Exam")
                    .examDate(LocalDateTime.now().plusDays(5))
                    .durationMinutes(120)
                    .build();

            when(examCoordinatorRepository.findByUser_Email(MOCK_EMAIL)).thenReturn(Optional.of(coordinator));
            when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
            when(instructorRepository.findById(2L)).thenReturn(Optional.of(instructor));
            when(examRepository.existsByCourse_CourseId(10L)).thenReturn(false);
            when(examRepository.save(any(Exam.class))).thenReturn(exam);
            when(examMapper.toExamOutputDTO(exam)).thenReturn(outputDTO);

            ExamOutputDTO result = examService.createExam(inputDTO);

            assertNotNull(result);
            assertEquals("Java Exam", result.getTitle());
        }
    }
}