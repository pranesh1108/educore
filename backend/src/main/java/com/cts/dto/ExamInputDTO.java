package com.cts.dto;

import com.cts.enumerate.AcademicTerm;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamInputDTO {

    @NotNull(message = "Course ID is required")
    @Positive(message = "Course ID must be a positive number")
    @Schema(example = "1")
    private Long courseId;

    @NotNull(message = "Instructor ID is required")
    @Positive(message = "Instructor ID must be a positive number")
    @Schema(example = "2")
    private Long instructorId;

    @NotBlank(message = "Exam title cannot be blank")
    @Size(min = 3, max = 150, message = "Exam title must be between 3 and 150 characters")
    @Pattern(
            regexp = "^[a-zA-Z0-9 ,.'\\-:()]{3,150}$",
            message = "Exam title contains invalid characters"
    )
    @Schema(example = "Midterm Examination")
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Exam date is required")
    @Future(message = "Exam date must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Schema(example = "2026-08-15 20:00", description = "Exam date in format: yyyy-MM-dd HH:mm (24-hour)")
    private LocalDateTime examDate;

    @NotNull(message = "Duration is required")
    @Min(value = 60, message = "Duration must be at least 60 minutes")
    @Max(value = 180, message = "Duration cannot exceed 180 minutes")
    private Integer durationMinutes;
}