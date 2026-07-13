package com.cts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegistrationInputDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 25, message = "Name must be between 2 and 25 characters")
    @Pattern(regexp = "^[a-zA-Z .'\\-]{2,25}$", message = "Name must contain only letters, spaces, apostrophes or hyphens")
    @Schema(example = "John Doe", description = "Full name containing only valid characters")
    private String name;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Password length must be between 8 and 20 characters")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!_\\-*./?]).{8,20}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    private String password;

    @NotBlank(message = "Role is required")
    @Pattern(
            regexp = "STUDENT|INSTRUCTOR|REGISTRAR|EXAM_COORDINATOR",
            message = "Invalid Role. Must be one of: STUDENT, INSTRUCTOR, REGISTRAR, EXAM_COORDINATOR"
    )
    private String role;

    @NotNull(message = "Phone number is required")
    @Min(value = 6000000000L, message = "Phone number must be a valid 10-digit mobile number starting with 6-9")
    @Max(value = 9999999999L, message = "Phone number must be a valid 10-digit mobile number")
    private Long phone;
}