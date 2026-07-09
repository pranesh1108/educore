package com.cts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassRoomOutputDTO {

    private Long roomId;
    private String roomName;
    private String location;
    private Integer capacity;
    private Integer roomNumber;
    private String status;
    private Long courseId;
    private String courseTitle;
    private LocalDateTime createdAt;

    // Students allocated to this room
    private List<AllocatedStudentDTO> allocatedStudents;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AllocatedStudentDTO {
        private Long studentId;
        private String studentName;
        private String enrollmentNumber;
        private String email;
    }
}
