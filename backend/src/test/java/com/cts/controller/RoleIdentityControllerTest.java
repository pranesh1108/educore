package com.cts.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.cts.service.UserService;

public class RoleIdentityControllerTest {

    @Mock private UserService userService;

    @InjectMocks
    private RoleIdentityController roleIdentityController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getRoleIdentity_success() {
        RoleIdentityDTO dto = RoleIdentityDTO.builder().roleId(5L).userId(1L).email("test@test.com").build();
        when(userService.getRoleIdentity("student", "test@test.com")).thenReturn(dto);

        ResponseEntity<RoleIdentityDTO> response = roleIdentityController.getRoleIdentity("student", "test@test.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5L, response.getBody().getRoleId());
    }
}