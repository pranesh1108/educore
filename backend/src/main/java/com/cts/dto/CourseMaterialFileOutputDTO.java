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
    private String type;       // "PDF" or "TEXT"
    private String textContent; // populated only when type = "TEXT"
    private LocalDateTime uploadedAt;
}
