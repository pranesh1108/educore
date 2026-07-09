package com.cts.serviceimpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.cts.dto.RegistrarCourseCreateDTO;
import com.cts.dto.RegistrarCourseResponseDTO;
import com.cts.entity.*;
import com.cts.enumerate.Role;
import com.cts.exception.*;
import com.cts.mapper.RegistrarMapper;
import com.cts.repository.*;

public class RegistrarAcademicServiceImplTest {

    @Mock private RegistrarMapper registrarMapper;
    @Mock private CourseRepository courseRepository;
    @Mock private InstructorRepository instructorRepository;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private RegistrarAcademicServiceImpl registrarAcademicService;

    private Instructor instructor;
    private Course course;
    private RegistrarCourseCreateDTO createDTO;
    private RegistrarCourseResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        User instructorUser = User.builder()
                .userId(1L).name("Aditya Patil").role(Role.INSTRUCTOR).status("ACTIVE").build();

        instructor = Instructor.builder()
                .instructorId(1L).user(instructorUser).skill(com.cts.enumerate.InstructorSkill.JAVA).experience(5).status("ACTIVE").build();

        course = Course.builder().courseId(1L).title("Java FSE").instructor(instructor).build();

        createDTO = RegistrarCourseCreateDTO.builder().title("Java FSE").instructorId(1L).build();
        responseDTO = RegistrarCourseResponseDTO.builder().courseId(1L).title("Java FSE").instructorName("Aditya Patil").build();
    }

    @Test
    void provisionNewCourse_success() {
        when(courseRepository.existsByTitleIgnoreCase("Java FSE")).thenReturn(false);
        when(instructorRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(courseRepository.save(any(Course.class))).thenReturn(course);
        when(registrarMapper.toRegistrarCourseResponseDTO(course)).thenReturn(responseDTO);

        RegistrarCourseResponseDTO result = registrarAcademicService.provisionNewCourse(createDTO);

        assertNotNull(result);
        assertEquals("Java FSE", result.getTitle());
    }
}