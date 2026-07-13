package com.cts.dto;

import com.cts.enumerate.AcademicTerm;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamOutputDTO {

    private Long examId;
    private String title;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd hh:mm a")
    private LocalDateTime examDate;

    private Integer durationMinutes;
    private Integer totalMarks;
    private Integer passingMarks;
    private Long courseId;
    private String courseTitle;
    private Long instructorId;
    private String instructorName;

    private Long roomId;
    private String roomName;
    private String roomLocation;
    private Integer roomNumber;

    @JsonFormat(pattern = "yyyy-MM-dd hh:mm a")
    private LocalDateTime createdAt;
}