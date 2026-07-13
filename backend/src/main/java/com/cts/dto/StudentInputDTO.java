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



    @Pattern(regexp = "^[a-zA-Z_, ]*$", message = "Field of interest contains invalid characters")
    private String fieldOfInterest;


    private String name;
    private String email;

}
