package com.cts.service;

import com.cts.dto.ExamRoomInputDTO;
import com.cts.dto.ExamRoomOutputDTO;
import java.util.List;

public interface ExamRoomService {

    ExamRoomOutputDTO createAndAllocate(ExamRoomInputDTO inputDTO);

    List<ExamRoomOutputDTO> getRoomsForExam(Long examId);
}