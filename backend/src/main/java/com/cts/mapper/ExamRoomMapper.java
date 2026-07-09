package com.cts.mapper;

import org.springframework.stereotype.Component;
import com.cts.dto.ExamRoomAllocationStudentDTO;
import com.cts.dto.ExamRoomOutputDTO;
import com.cts.entity.ExamRoom;
import com.cts.entity.ExamRoomAllocation;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ExamRoomMapper {

    public ExamRoomOutputDTO toOutputDTO(ExamRoom room,
                                          List<ExamRoomAllocation> allocations) {
        List<ExamRoomAllocationStudentDTO> studentDTOs = allocations.stream()
                .map(this::toStudentDTO)
                .collect(Collectors.toList());

        return ExamRoomOutputDTO.builder()
                .roomId(room.getRoomId())
                .roomName(room.getRoomName())
                .location(room.getLocation())
                .capacity(room.getCapacity())
                .roomNumber(room.getRoomNumber())
                .examId(room.getExam().getExamId())
                .examTitle(room.getExam().getTitle())
                .term(room.getExam().getTerm())
                .examDate(room.getExam().getExamDate())
                .studentsAllocated(studentDTOs.size())
                .students(studentDTOs)
                .createdAt(room.getCreatedAt())
                .build();
    }

    public ExamRoomAllocationStudentDTO toStudentDTO(ExamRoomAllocation allocation) {
        return ExamRoomAllocationStudentDTO.builder()
                .allocationId(allocation.getAllocationId())
                .studentId(allocation.getStudent().getStudentId())
                .studentName(allocation.getStudent().getUser().getName())
                .email(allocation.getStudent().getUser().getEmail())
                .build();
    }
}
