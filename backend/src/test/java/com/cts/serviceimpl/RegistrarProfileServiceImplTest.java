package com.cts.serviceimpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.cts.entity.*;
import com.cts.enumerate.Role;
import com.cts.repository.*;

public class RegistrarProfileServiceImplTest {

    @Mock private RegistrarRepository registrarRepository;
    @Mock private CourseRepository courseRepository;

    @InjectMocks
    private RegistrarProfileServiceImpl registrarProfileService;

    private User user;
    private Registrar registrar;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = User.builder()
                .userId(1L).name("Meera Nair").email("registrar1@educore.com")
                .role(Role.REGISTRAR).status("ACTIVE").build();

        registrar = Registrar.builder().registrarId(1L).user(user).build();
    }

    @Test
    void getRegistrarProfile_success() {
        when(registrarRepository.findByUserUserId(1L)).thenReturn(Optional.of(registrar));
        when(courseRepository.countByInstructor_InstructorIdIsNotNull()).thenReturn(3);

        RegistrarOutputDTO result = registrarProfileService.getRegistrarProfile(1L);

        assertNotNull(result);
        assertEquals("Meera Nair", result.getName());
        assertEquals(3, result.getPublishedCourseCount());
    }
}