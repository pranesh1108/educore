package com.cts.serviceimpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import com.cts.dto.ExamResultInputDTO;
import com.cts.dto.ExamResultOutputDTO;
import com.cts.entity.*;
import com.cts.enumerate.ExamResult;
import com.cts.repository.*;
import com.cts.util.SecurityUtils;

public class ExamResultServiceImplTest {

    @Mock private ExamResultRepository examResultRepository;
    @Mock private ExamRepository examRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private CourseEnrollmentRepository enrollmentRepository;

    @InjectMocks
    private ExamResultServiceImpl examResultService;

    private final String MOCK_EMAIL = "student@educore.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void publishResult_success_pass() {
        ExamResultInputDTO input = ExamResultInputDTO.builder().examId(1L).studentId(10L).courseId(100L).score(85.0).build();
        Exam exam = Exam.builder().examId(1L).title("Final Exam").build();
        User user = User.builder().name("Ravi").build();
        Student student = Student.builder().studentId(10L).user(user).build();
        Course course = Course.builder().courseId(100L).title("Java").build();

        ExamResultEntity saved = ExamResultEntity.builder()
                .resultId(1L).exam(exam).student(student).course(course).score(85.0).result(ExamResult.PASS).build();

        when(examResultRepository.existsByExam_ExamIdAndStudent_StudentId(1L, 10L)).thenReturn(false);
        when(examRepository.findById(1L)).thenReturn(Optional.of(exam));
        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(100L)).thenReturn(Optional.of(course));
        when(examResultRepository.save(any(ExamResultEntity.class))).thenReturn(saved);

        ExamResultOutputDTO output = examResultService.publishResult(input);

        assertNotNull(output);
        assertEquals(ExamResult.PASS, output.getResult());
        assertEquals(85.0, output.getScore());
    }

    @Test
    void getResultsByStudent_success() {
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getLoggedInEmail).thenReturn(MOCK_EMAIL);

            User user = User.builder().name("Ravi").build();
            Student student = Student.builder().studentId(10L).user(user).build();
            Exam exam = Exam.builder().examId(1L).title("Final Exam").build();
            Course course = Course.builder().courseId(100L).title("Java").build();

            ExamResultEntity entity = ExamResultEntity.builder()
                    .resultId(1L).exam(exam).student(student).course(course).score(80.0).result(ExamResult.PASS).build();

            when(studentRepository.findByUser_Email(MOCK_EMAIL)).thenReturn(Optional.of(student));
            when(examResultRepository.findByStudent_StudentId(10L)).thenReturn(List.of(entity));

            List<ExamResultOutputDTO> results = examResultService.getResultsByStudent();

            assertNotNull(results);
            assertEquals(1, results.size());
        }
    }
}