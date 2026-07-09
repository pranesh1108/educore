package com.cts.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamRoomAllocationStudentDTO {

    private Long allocationId;
    private Long studentId;
    private String studentName;
    private String email;
}
