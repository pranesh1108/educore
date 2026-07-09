package com.cts.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrarProfileCreateDTO {

    @NotNull(message = "User ID is required to link a registrar profile")
    private Long userId;
}