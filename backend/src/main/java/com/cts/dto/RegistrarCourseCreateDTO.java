package com.cts.dto;

import com.cts.enumerate.Prerequisite;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrarCourseCreateDTO {

    @NotBlank(message = "Course title cannot be blank")
    @Size(min = 3, max = 100, message = "Course title must be between 3 and 100 characters")
    @Pattern(
            regexp = "^[a-zA-Z0-9 ,.'\\-:()]{3,100}$",
            message = "Course title contains invalid characters"
    )
    @Schema(example = "Introduction to Software Engineering")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    private Prerequisite prerequisite;

    @NotNull(message = "Course start date is required")
    @FutureOrPresent(message = "Course start date cannot be set in the past")
    @Schema(example = "2026-08-01", description = "The starting date of the course track")
    private LocalDate startDate;

    @NotNull(message = "Course end date is required")
    @FutureOrPresent(message = "Course end date cannot be set in the past")
    @Schema(example = "2026-11-15", description = "The completion date of the course track")
    private LocalDate endDate;

    @NotNull(message = "Enrollment deadline date is required")
    @Schema(example = "2026-07-25", description = "The deadline date after which no student can enroll or gain entry")
    private LocalDate enrollmentDeadlineDate;

    private String syllabusPath;

    @NotNull(message = "Instructor ID is required")
    @Positive(message = "Instructor ID must be a positive number")
    private Long instructorId;
}