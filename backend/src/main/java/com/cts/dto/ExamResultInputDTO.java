package com.cts.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamResultInputDTO {

    @NotNull(message = "Exam ID is required")
    @Positive(message = "Exam ID must be a positive number")
    private Long examId;

    @NotNull(message = "Student ID is required")
    @Positive(message = "Student ID must be a positive number")
    private Long studentId;

    @NotNull(message = "Course ID is required")
    @Positive(message = "Course ID must be a positive number")
    private Long courseId;

    @NotNull(message = "Score is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Score cannot be less than 0")
    @DecimalMax(value = "100.0", inclusive = true, message = "Score cannot exceed 100")
    private Double score;
}