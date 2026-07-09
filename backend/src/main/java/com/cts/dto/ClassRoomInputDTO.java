package com.cts.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassRoomInputDTO {

    @NotNull(message = "Course ID is required")
    @Positive(message = "Course ID must be a positive number")
    private Long courseId;

    @NotBlank(message = "Room name is required")
    @Size(max = 100, message = "Room name cannot exceed 100 characters")
    @Pattern(
        regexp = "^[a-zA-Z0-9 ,.'\\-()]{1,100}$",
        message = "Room name contains invalid characters"
    )
    private String roomName;

    @NotBlank(message = "Location is required")
    @Size(max = 255, message = "Location cannot exceed 255 characters")
    @Pattern(
        regexp = "^[a-zA-Z0-9 ,.'\\-/()]{1,255}$",
        message = "Location contains invalid characters"
    )
    private String location;

    @NotNull(message = "Room number is required")
    @Positive(message = "Room number must be a positive number")
    private Integer roomNumber;
}
