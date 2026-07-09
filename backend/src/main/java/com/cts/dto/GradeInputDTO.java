package com.cts.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeInputDTO {

    @NotNull(message = "Grade is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Grade cannot be less than 0")
    @DecimalMax(value = "100.0", inclusive = true, message = "Grade cannot exceed 100")
    private Double grade;

    @Size(max = 1000, message = "Feedback cannot exceed 1000 characters")
    private String feedback;
}