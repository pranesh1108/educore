package com.cts.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.cts.entity.Instructor;
import com.cts.entity.User;
import com.cts.repository.InstructorRepository;
import com.cts.service.CourseService;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class InstructorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    @MockBean
    private InstructorRepository instructorRepository;

    @Test
    // This mocks the authentication context so SecurityUtils returns "instructor@educore.com"
    @WithMockUser(username = "instructor@educore.com", roles = {"INSTRUCTOR"})
    public void getAssignedCourses_success_returns200() throws Exception {
        // Arrange
        Long mockInstructorId = 1L;
        User mockUser = User.builder().email("instructor@educore.com").build();
        Instructor mockInstructor = Instructor.builder().instructorId(mockInstructorId).user(mockUser).build();

        // Mock the internal repository check inside resolveContextInstructorId()
        when(instructorRepository.findByUser_Email("instructor@educore.com"))
                .thenReturn(Optional.of(mockInstructor));

        // Mock the core course service behavior
        when(courseService.getAssignedCourses(mockInstructorId))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        // REMOVED the parameter from the URL path / query parameter structure completely!
        mockMvc.perform(get("/api/v1/instructor/my-courses")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "instructor@educore.com", roles = {"INSTRUCTOR"})
    public void getAssignedCourses_noCourses_propagatesException() throws Exception {
        // Arrange
        Long mockInstructorId = 1L;
        User mockUser = User.builder().email("instructor@educore.com").build();
        Instructor mockInstructor = Instructor.builder().instructorId(mockInstructorId).user(mockUser).build();

        when(instructorRepository.findByUser_Email("instructor@educore.com"))
                .thenReturn(Optional.of(mockInstructor));

        // Mock an exception case or empty failure state here depending on your target exception handler
        when(courseService.getAssignedCourses(mockInstructorId))
                .thenThrow(new com.cts.exception.CourseNotFoundException("No courses assigned"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/instructor/my-courses")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Verification matches custom mapper configs
    }
}