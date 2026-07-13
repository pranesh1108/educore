package com.cts.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;
import com.cts.enumerate.Role;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDTO {

    private Long userId;
    private String email;
    private String name;
    private Role role;
    private Long phone;
    private LocalDate createdAt;
    private LocalDateTime lastLoginAt;
}
