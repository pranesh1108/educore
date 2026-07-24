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

import com.cts.dto.CourseOutputDTO;
import com.cts.entity.Course;
import com.cts.entity.Instructor;
import com.cts.mapper.CourseMapper;
import com.cts.repository.CourseRepository;
import com.cts.repository.InstructorRepository;
import com.cts.util.SecurityUtils;

public class CourseServiceImplTest {

    @Mock private CourseMapper courseMapper;
    @Mock private CourseRepository courseRepository;
    @Mock private InstructorRepository instructorRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    private final String MOCK_EMAIL = "instructor@educore.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAssignedCourses_success() {
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getLoggedInEmail).thenReturn(MOCK_EMAIL);

            Instructor mockInstructor = Instructor.builder().instructorId(1L).build();
            Course mockCourse = Course.builder().courseId(10L).title("Java").build();
            CourseOutputDTO dto = CourseOutputDTO.builder().courseId(10L).title("Java").build();

            when(instructorRepository.findByUser_Email(MOCK_EMAIL)).thenReturn(Optional.of(mockInstructor));
            when(courseRepository.findByInstructor_InstructorId(1L)).thenReturn(List.of(mockCourse));
            when(courseMapper.toCourseOutputDTO(mockCourse)).thenReturn(dto);

            List<CourseOutputDTO> results = courseService.getAssignedCourses();

            assertNotNull(results);
            assertEquals(1, results.size());
            assertEquals("Java", results.get(0).getTitle());
        }
    }
}