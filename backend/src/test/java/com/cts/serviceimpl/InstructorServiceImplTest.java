package com.cts.serviceimpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import com.cts.dto.EnrollmentOutputDTO;
import com.cts.dto.InstructorInputDTO;
import com.cts.dto.InstructorOutputDTO;
import com.cts.entity.Course;
import com.cts.entity.Instructor;
import com.cts.mapper.InstructorMapper;
import com.cts.mapper.StudentMapper;
import com.cts.repository.CourseEnrollmentRepository;
import com.cts.repository.CourseRepository;
import com.cts.repository.InstructorRepository;
import com.cts.util.SecurityUtils;
import java.util.Collections;
public class InstructorServiceImplTest {

    @Mock private InstructorMapper instructorMapper;
    @Mock private StudentMapper studentMapper;
    @Mock private InstructorRepository instructorRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private CourseEnrollmentRepository enrollmentRepository;

    @InjectMocks
    private InstructorServiceImpl instructorService;

    private final String MOCK_EMAIL = "instructor@educore.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void updateInstructorProfile_success() {
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getLoggedInEmail).thenReturn(MOCK_EMAIL);

            Instructor instructor = Instructor.builder().instructorId(1L).build();
            InstructorInputDTO inputDTO = InstructorInputDTO.builder()
                    .experience(5)
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .build();
            InstructorOutputDTO outputDTO = InstructorOutputDTO.builder().experience(5).build();

            when(instructorRepository.findByUser_Email(MOCK_EMAIL)).thenReturn(Optional.of(instructor));
            when(instructorRepository.save(instructor)).thenReturn(instructor);
            when(instructorMapper.toInstructorOutputDTO(instructor)).thenReturn(outputDTO);

            InstructorOutputDTO result = instructorService.updateInstructorProfile(inputDTO);

            assertNotNull(result);
            assertEquals(5, result.getExperience());
        }
    }

    @Test
    void getEnrolledStudents_success() {
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getLoggedInEmail).thenReturn(MOCK_EMAIL);

            Instructor instructor = Instructor.builder().instructorId(1L).build();
            Course course = Course.builder().courseId(10L).build();

            when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
            when(instructorRepository.findByUser_Email(MOCK_EMAIL)).thenReturn(Optional.of(instructor));
            when(courseRepository.findByCourseIdAndInstructor_InstructorId(10L, 1L)).thenReturn(Optional.of(course));
            when(enrollmentRepository.findByCourse_CourseId(10L)).thenReturn(Collections.emptyList());

            List<EnrollmentOutputDTO> results = instructorService.getEnrolledStudents(10L);

            assertNotNull(results);
            assertTrue(results.isEmpty());
        }
    }
}