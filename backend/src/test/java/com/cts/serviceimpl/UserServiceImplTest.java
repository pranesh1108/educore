package com.cts.serviceimpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cts.dto.*;
import com.cts.entity.*;
import com.cts.enumerate.Role;
import com.cts.repository.*;
import com.cts.util.JwtUtil;

public class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private InstructorRepository instructorRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private RegistrarRepository registrarRepository;
    @Mock private ExamCoordinatorRepository examCoordinatorRepository;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addUser_student_success() {
        RegistrationInputDTO input = RegistrationInputDTO.builder()
                .email("student@test.com")
                .name("Ravi")
                .password("pass123")
                .role("STUDENT")
                .phone(9876543210L)
                .build();

        User savedUser = User.builder()
                .userId(1L)
                .email("student@test.com")
                .name("Ravi")
                .role(Role.STUDENT)
                .build();

        when(userRepository.existsByEmail("student@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("hashedPass");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        RegistrationOutputDTO result = userService.addUser(input);

        assertNotNull(result);
        assertEquals("student@test.com", result.getEmail());
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void userLogin_success() {
        LoginDTO loginDTO = LoginDTO.builder().email("student@test.com").password("pass123").build();
        User user = User.builder().userId(1L).email("student@test.com").name("Ravi").password("hashedPass").role(Role.STUDENT).build();

        when(userRepository.findByEmail("student@test.com")).thenReturn(user);
        when(passwordEncoder.matches("pass123", "hashedPass")).thenReturn(true);
        when(jwtUtil.generateToken("student@test.com", "STUDENT")).thenReturn("generated.jwt.token");

        LoginResponseDTO response = userService.userLogin(loginDTO);

        assertNotNull(response);
        assertEquals("generated.jwt.token", response.getToken());
    }
}