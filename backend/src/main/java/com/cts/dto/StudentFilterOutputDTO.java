package com.cts.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentFilterOutputDTO {

    private Long studentId;
    private String name;
    private String email;
    private String fieldOfInterest;
    private String educationLevel;
    private String status;
    private List<String> enrolledCourses;
}
