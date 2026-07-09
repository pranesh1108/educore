package com.cts.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleIdentityDTO {

    // roleId = registrarId / instructorId / studentId / coordinatorId
    private Long roleId;
    private Long userId;
    private String email;
}