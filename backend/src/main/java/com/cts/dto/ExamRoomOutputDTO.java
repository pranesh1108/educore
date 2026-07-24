package com.cts.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamRoomOutputDTO {

    private Long roomId;
    private String roomName;
    private String location;
    private Integer capacity;
    private Integer roomNumber;

    private Long examId;
    private String examTitle;

    @JsonFormat(pattern = "yyyy-MM-dd hh:mm a")
    private LocalDateTime examDate;

    private Integer studentsAllocated;

    private List<ExamRoomAllocationStudentDTO> students;

    @JsonFormat(pattern = "yyyy-MM-dd hh:mm a")
    private LocalDateTime createdAt;

}
