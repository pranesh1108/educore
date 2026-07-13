package com.cts.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentOutputDTO {

    private Long enrollmentId;
    private Long courseId;
    private String courseTitle;
    private String courseDescription;
    private String courseDuration;
    private String instructorName;
    private Long studentId;
    private String studentName;
    private String enrollmentNumber;
    private LocalDate enrolledAt;
    private LocalDate startDate;
    private LocalDate endDate;
}
