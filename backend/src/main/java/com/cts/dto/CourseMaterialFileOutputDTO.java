package com.cts.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseMaterialFileOutputDTO {

    private Long fileId;
    private Long courseId;
    private String courseTitle;
    private String fileName;
    private String type;
    private String textContent;
    private LocalDateTime uploadedAt;
}
