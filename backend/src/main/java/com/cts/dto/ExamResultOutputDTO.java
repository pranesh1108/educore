package com.cts.dto;

import com.cts.enumerate.ExamResult;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamResultOutputDTO {

    private Long resultId;
    private Long examId;
    private String examTitle;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseTitle;
    private Double score;
    private ExamResult result;
    private LocalDateTime publishedAt;
    private String message;
}
