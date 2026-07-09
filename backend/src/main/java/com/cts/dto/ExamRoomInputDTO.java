package com.cts.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamRoomInputDTO {

    @NotNull(message = "Physical room ID is required")
    @Positive(message = "Physical room ID must be a positive number")
    private Long physicalRoomId;

    @NotNull(message = "Exam ID is required")
    @Positive(message = "Exam ID must be a positive number")
    private Long examId;

    private Integer roomNumber;
}
