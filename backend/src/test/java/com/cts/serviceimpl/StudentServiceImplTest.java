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
import org.mockito.MockitoAnnotations;

import com.cts.dto.*;
import com.cts.entity.*;
import com.cts.enumerate.Role;
import com.cts.mapper.StudentMapper;
import com.cts.repository.*;

public class StudentServiceImplTest {

    @Mock private StudentMapper studentMapper;
    @Mock private StudentRepository studentRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private CourseEnrollmentRepository enrollmentRepository;

    @InjectMocks
    private StudentServiceImpl studentService;

    private Student student;
    private Course course;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        User studentUser = User.builder().userId(1L).name("Ravi Kumar").role(Role.STUDENT).build();
        student = Student.builder().studentId(1L).user(studentUser).status("ACTIVE").build();
        course = Course.builder().courseId(10L).title("Java FSE").build();
    }

    @Test
    void updateStudentProfile_success() {
        StudentInputDTO input = StudentInputDTO.builder()
                .userId(1L).educationLevel("BACHELOR").dateOfBirth(LocalDate.of(2000, 1, 1)).build();

        StudentOutputDTO output = StudentOutputDTO.builder()
                .studentId(1L).educationLevel("BACHELOR").build();

        when(studentRepository.findByUser_UserId(1L)).thenReturn(Optional.of(student));
        when(studentRepository.save(student)).thenReturn(student);
        when(studentMapper.tostudentOutputDTO(student)).thenReturn(output);

        StudentOutputDTO result = studentService.updateStudentProfile(input);

        assertNotNull(result);
        assertEquals("BACHELOR", result.getEducationLevel());
    }
}