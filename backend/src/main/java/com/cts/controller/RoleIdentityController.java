package com.cts.controller;

import com.cts.dto.RoleIdentityDTO;
import com.cts.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Role Identity", description = "Look up roleId by role and email")
public class RoleIdentityController {

    private final UserService userService;

    @Operation(
        summary = "Get role ID by role and email",
        description = "Returns the roleId (registrarId / studentId / instructorId / coordinatorId) "
                    + "and email for the given role and email. "
                    + "Role path values: registrar, student, instructor, exam_coordinator. "
                    + "Requires a valid JWT (any role)."
    )
    @GetMapping("/{role}/{email}")
    public ResponseEntity<RoleIdentityDTO> getRoleIdentity(
            @PathVariable String role,
            @PathVariable String email) {
        return new ResponseEntity<>(
                userService.getRoleIdentity(role, email), HttpStatus.OK);
    }
}
