package com.cts.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrarProfileResponseDTO {

    private Long registrarId;
    private Long userId;
    private String email;
    private String name;
    private String status;
}