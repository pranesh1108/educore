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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cts.dto.*;
import com.cts.entity.*;
import com.cts.enumerate.Role;
import com.cts.exception.*;
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

    private User savedUser;
    private RegistrationInputDTO inputDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        inputDTO = RegistrationInputDTO.builder()
                .email("aditya@test.com")
                .name("Aditya Patil")
                .password("pass123")
                .role("INSTRUCTOR")
                .phone(9876543210L)
                .build();

        savedUser = User.builder()
                .userId(1L)
                .email("aditya@test.com")
                .name("Aditya Patil")
                .role(Role.INSTRUCTOR)
                .phone(9876543210L)
                .status("ACTIVE")
                .build();
    }

    // ── ADD USER TESTS ────────────────────────────────────────────────

    @Test
    void addUser_instructor_success_createsInstructorProfile() {
        when(userRepository.existsByEmail("aditya@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(instructorRepository.save(any(Instructor.class))).thenReturn(new Instructor());

        RegistrationOutputDTO result = userService.addUser(inputDTO);

        assertNotNull(result);
        assertEquals("aditya@test.com", result.getEmail());
        assertEquals(Role.INSTRUCTOR, result.getRole());
        verify(instructorRepository).save(any(Instructor.class));
    }

    @Test
    void addUser_duplicateEmail_throwsInvalidEmailException() {
        when(userRepository.existsByEmail("aditya@test.com")).thenReturn(true);

        assertThrows(InvalidEmailException.class, () -> userService.addUser(inputDTO));
        verify(userRepository, never()).save(any(User.class));
    }

    // ── USER LOGIN TESTS ──────────────────────────────────────────────

    @Test
    void userLogin_success_returnsTokenAndInfo() {
        LoginDTO loginDTO = LoginDTO.builder()
                .email("aditya@test.com")
                .password("pass123")
                .build();

        when(userRepository.findByEmail("aditya@test.com")).thenReturn(savedUser);
        when(passwordEncoder.matches("pass123", savedUser.getPassword())).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken("aditya@test.com", "INSTRUCTOR")).thenReturn("mocked-token");

        LoginResponseDTO result = userService.userLogin(loginDTO);

        assertNotNull(result);
        assertEquals("mocked-token", result.getToken());
    }

    @Test
    void userLogin_wrongPassword_throwsUserNotFoundException() {
        LoginDTO loginDTO = LoginDTO.builder()
                .email("aditya@test.com")
                .password("wrong_password")
                .build();

        when(userRepository.findByEmail("aditya@test.com")).thenReturn(savedUser);
        when(passwordEncoder.matches("wrong_password", savedUser.getPassword())).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userService.userLogin(loginDTO));
    }

    // ── GET ROLE IDENTITY TESTS ───────────────────────────────────────

    @Test
    void getRoleIdentity_instructor_success() {
        Instructor mockInstructor = Instructor.builder()
                .instructorId(88L)
                .user(savedUser)
                .build();

        when(userRepository.findByEmail("aditya@test.com")).thenReturn(savedUser);
        when(instructorRepository.findByUser_Email("aditya@test.com")).thenReturn(Optional.of(mockInstructor));

        RoleIdentityDTO result = userService.getRoleIdentity("instructor", "aditya@test.com");

        assertNotNull(result);
        assertEquals(88L, result.getRoleId());
        assertEquals(1L, result.getUserId());
        assertEquals("aditya@test.com", result.getEmail());
    }

    @Test
    void getRoleIdentity_invalidEmail_throwsInvalidEmailException() {
        assertThrows(InvalidEmailException.class, () -> userService.getRoleIdentity("student", "invalid-email"));
    }

    @Test
    void getRoleIdentity_userNotFound_throwsUserNotFoundException() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> userService.getRoleIdentity("student", "unknown@test.com"));
    }
}