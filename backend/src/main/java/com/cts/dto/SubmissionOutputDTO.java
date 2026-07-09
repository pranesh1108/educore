package com.cts.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionOutputDTO {

    private Long submissionId;
    private Long studentId;
    private String studentName;
    private String enrollmentNumber;
    private String fileName;
    private LocalDateTime submittedAt;
    private String status;
    private Double grade;
    private String feedback;
    private Long assignmentId;
    private String assignmentTitle;
    private Long courseId;
    private String courseTitle;
}
