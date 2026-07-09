package com.cts.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhysicalRoomOutputDTO {

    private Long roomId;
    private String roomName;
    private String location;
    private Integer capacity;

    // AVAILABLE or OCCUPIED
    private String status;

    // Populated when OCCUPIED
    private Long assignedExamId;

    @JsonFormat(pattern = "yyyy-MM-dd hh:mm a")
    private LocalDateTime assignedFrom;

    @JsonFormat(pattern = "yyyy-MM-dd hh:mm a")
    private LocalDateTime assignedUntil;

    @JsonFormat(pattern = "yyyy-MM-dd hh:mm a")
    private LocalDateTime createdAt;
}
