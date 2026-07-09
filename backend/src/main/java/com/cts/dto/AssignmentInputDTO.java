package com.cts.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentInputDTO {

    @NotNull(message = "Course ID is required")
    @Positive(message = "Course ID must be a positive number")
    private Long courseId;

    @NotBlank(message = "Assignment title is required")
    @Size(min = 3, max = 150, message = "Title must be between 3 and 150 characters")
    @Pattern(
        regexp = "^[a-zA-Z0-9 ,.'\\-:()]{3,150}$",
        message = "Title contains invalid characters"
    )
    private String title;

    @Size(max = 2000, message = "Instructions cannot exceed 2000 characters")
    private String instructions;

    @NotNull(message = "Total marks is required")
    @DecimalMin(value = "1.0", message = "Total marks must be at least 1")
    @DecimalMax(value = "100.0", message = "Total marks cannot exceed 100")
    private Double totalMarks;

    @NotNull(message = "Due date is required")
    @Future(message = "Due date must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Schema(example = "2026-08-30 23:59", description = "Assignment due date in format: yyyy-MM-dd HH:mm")
    private LocalDateTime dueDate;
}
