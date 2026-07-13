package com.cts.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cts.dto.LoginDTO;
import com.cts.dto.LoginResponseDTO;
import com.cts.dto.RegistrationInputDTO;
import com.cts.dto.RegistrationOutputDTO;
import com.cts.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/user")
@Tag(name = "User", description = "User registration and login endpoints")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Register a new user",
            description = "Creates a user account and an empty role-specific profile. "
                    + "Supported roles: STUDENT, INSTRUCTOR, REGISTRAR, EXAM_COORDINATOR."
    )
    @PostMapping("/register")
    public ResponseEntity<RegistrationOutputDTO> addUser(
            @Valid @RequestBody RegistrationInputDTO registerRequestDTO) {
        return new ResponseEntity<>(userService.addUser(registerRequestDTO), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Login and receive a JWT token",
            description = "Validates email + password, then returns a JWT. "
                    + "Use the token in the Authorization header for all secured requests."
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> userLogin(
            @Valid @RequestBody LoginDTO loginDTO) {
        return new ResponseEntity<>(userService.userLogin(loginDTO), HttpStatus.OK);
    }
}
