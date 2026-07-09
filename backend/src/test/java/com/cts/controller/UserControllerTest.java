package com.cts.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.cts.dto.*;
import com.cts.enumerate.Role;
import com.cts.service.UserService;

public class UserControllerTest {

    @Mock private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addUser_student_success_returns201() {
        RegistrationInputDTO input = RegistrationInputDTO.builder()
                .email("ravi@test.com").name("Ravi Kumar").password("pass123").role("STUDENT").phone(9876543210L).build();

        RegistrationOutputDTO output = RegistrationOutputDTO.builder()
                .userId(1L).email("ravi@test.com").role(Role.STUDENT).phone(9876543210L).status("ACTIVE").createdAt(LocalDate.now()).build();

        when(userService.addUser(any(RegistrationInputDTO.class))).thenReturn(output);

        ResponseEntity<RegistrationOutputDTO> response = userController.addUser(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("ravi@test.com", response.getBody().getEmail());
    }

    @Test
    void userLogin_success_returns200WithToken() {
        LoginDTO input = LoginDTO.builder().email("ravi@test.com").password("pass123").build();
        LoginResponseDTO output = LoginResponseDTO.builder()
                .email("ravi@test.com").role("STUDENT").phone(9876543210L).token("mockToken").build();

        when(userService.userLogin(any(LoginDTO.class))).thenReturn(output);

        ResponseEntity<LoginResponseDTO> response = userController.userLogin(input);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("mockToken", response.getBody().getToken());
    }
}