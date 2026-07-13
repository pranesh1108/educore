package com.cts.dto;

import com.cts.enumerate.InstructorSkill;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorInputDTO {


    @NotEmpty(message = "At least one skill must be provided")
    private List<InstructorSkill> skills;

    @NotNull(message = "Experience is required")
    @Min(value = 0, message = "Experience cannot be negative")
    @Max(value = 50, message = "Experience cannot exceed 50 years")
    private Integer experience;

    @Past(message = "Date of birth must be a past date")
    private LocalDate dateOfBirth;
}