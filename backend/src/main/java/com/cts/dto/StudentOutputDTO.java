package com.cts.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentOutputDTO {

    private Long studentId;
    private LocalDate dateOfBirth;
    private String fieldOfInterest;
    private Long userId;
    private String name;
    private String email;
    private Long phone;
    private String role;
}
