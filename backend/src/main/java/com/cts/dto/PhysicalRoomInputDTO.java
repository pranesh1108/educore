package com.cts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhysicalRoomInputDTO {

    @NotBlank(message = "Room name is required")
    @Size(min = 2, max = 100, message = "Room name must be between 2 and 100 characters")
    @Pattern(
            regexp = "^[a-zA-Z0-9 ,.'\\-()]{2,100}$",
            message = "Room name contains invalid characters"
    )
    @Schema(example = "Hall A")
    private String roomName;

    @NotBlank(message = "Location is required")
    @Size(min = 2, max = 255, message = "Location must be between 2 and 255 characters")
    @Pattern(
            regexp = "^[a-zA-Z0-9 ,.'\\-/()]{2,255}$",
            message = "Location contains invalid characters"
    )
    @Schema(example = "Floor 1 Building A")
    private String location;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 200, message = "Capacity cannot exceed 200")
    private Integer capacity;
}