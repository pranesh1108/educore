package com.cts.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentInputDTO {



    @Past(message = "Date of birth must be a past date")
    private LocalDate dateOfBirth;


    @Size(min = 2, max = 150, message = "Field of interest must be between 2 and 150 characters")
    @Pattern(
        regexp = "^[a-zA-Z0-9 ,.'&()\\-]{2,150}$",
        message = "Field of interest contains invalid characters"
    )
    private String fieldOfInterest;
}
