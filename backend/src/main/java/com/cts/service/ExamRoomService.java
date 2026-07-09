package com.cts.service;

import com.cts.dto.ExamRoomInputDTO;
import com.cts.dto.ExamRoomOutputDTO;
import java.util.List;

public interface ExamRoomService {

    // Create room + assign to exam + auto-allocate batch of students
    ExamRoomOutputDTO createAndAllocate(ExamRoomInputDTO inputDTO);

    // View all rooms for a specific exam
    List<ExamRoomOutputDTO> getRoomsForExam(Long examId);
}