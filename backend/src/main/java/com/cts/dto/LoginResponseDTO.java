package com.cts.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponseDTO {

    private Long userId;
    private String email;
    private String userName;
    private String role;
    private Long phone;
    private String token;
}
