package com.cts.serviceimpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import com.cts.dto.EnrollmentOutputDTO;
import com.cts.dto.StudentInputDTO;
import com.cts.dto.StudentOutputDTO;
import com.cts.entity.*;
import com.cts.mapper.StudentMapper;
import com.cts.repository.*;
import com.cts.util.SecurityUtils;

public class StudentServiceImplTest {

    @Mock private StudentMapper studentMapper;
    @Mock private StudentRepository studentRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private CourseEnrollmentRepository enrollmentRepository;

    @InjectMocks
    private StudentServiceImpl studentService;

    private final String MOCK_EMAIL = "student@educore.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void updateStudentProfile_success() {
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getLoggedInEmail).thenReturn(MOCK_EMAIL);

            Student student = Student.builder().studentId(1L).build();
            StudentInputDTO inputDTO = StudentInputDTO.builder()
                    .dateOfBirth(LocalDate.of(2000, 1, 1))
                    .fieldOfInterest("JAVA")
                    .build();
            StudentOutputDTO outputDTO = StudentOutputDTO.builder().fieldOfInterest("JAVA").build();

            when(studentRepository.findByUser_Email(MOCK_EMAIL)).thenReturn(Optional.of(student));
            when(studentRepository.save(student)).thenReturn(student);
            when(studentMapper.tostudentOutputDTO(student)).thenReturn(outputDTO);

            StudentOutputDTO result = studentService.updateStudentProfile(inputDTO);

            assertNotNull(result);
            assertEquals("JAVA", result.getFieldOfInterest());
        }
    }

    @Test
    void enrollInCourse_success() {
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getLoggedInEmail).thenReturn(MOCK_EMAIL);

            Student student = Student.builder().studentId(1L).build();
            Course course = Course.builder().courseId(10L).title("Java").build();
            CourseEnrollment saved = CourseEnrollment.builder().enrollmentId(100L).build();
            EnrollmentOutputDTO outputDTO = EnrollmentOutputDTO.builder().enrollmentId(100L).courseTitle("Java").build();

            when(studentRepository.findByUser_Email(MOCK_EMAIL)).thenReturn(Optional.of(student));
            when(enrollmentRepository.existsByStudent_StudentIdAndCourse_CourseId(1L, 10L)).thenReturn(false);
            when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
            when(enrollmentRepository.save(any(CourseEnrollment.class))).thenReturn(saved);
            when(studentMapper.toEnrollmentOutputDTO(saved)).thenReturn(outputDTO);

            EnrollmentOutputDTO result = studentService.enrollInCourse(10L);

            assertNotNull(result);
            assertEquals("Java", result.getCourseTitle());
        }
    }
}