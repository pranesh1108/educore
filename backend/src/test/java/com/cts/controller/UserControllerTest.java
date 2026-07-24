package com.cts.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    void addUser_success() {
        RegistrationInputDTO input = RegistrationInputDTO.builder().email("test@educore.com").role("STUDENT").build();
        RegistrationOutputDTO output = RegistrationOutputDTO.builder().userId(1L).email("test@educore.com").role(Role.STUDENT).build();

        when(userService.addUser(any(RegistrationInputDTO.class))).thenReturn(output);

        ResponseEntity<RegistrationOutputDTO> response = userController.addUser(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("test@educore.com", response.getBody().getEmail());
    }

    @Test
    void userLogin_success() {
        LoginDTO input = LoginDTO.builder().email("test@educore.com").password("pass").build();
        LoginResponseDTO output = LoginResponseDTO.builder().email("test@educore.com").token("jwt.token.here").build();

        when(userService.userLogin(any(LoginDTO.class))).thenReturn(output);

        ResponseEntity<LoginResponseDTO> response = userController.userLogin(input);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("jwt.token.here", response.getBody().getToken());
    }
}