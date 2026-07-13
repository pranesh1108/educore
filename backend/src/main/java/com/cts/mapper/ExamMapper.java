package com.cts.mapper;

import org.springframework.stereotype.Component;
import com.cts.dto.ExamOutputDTO;
import com.cts.entity.Exam;
import com.cts.entity.ExamRoom;
import com.cts.repository.ExamRoomRepository;
import lombok.AllArgsConstructor;
import java.util.List;

@Component
@AllArgsConstructor
public class ExamMapper {

    private final ExamRoomRepository examRoomRepository;

    public ExamOutputDTO toExamOutputDTO(Exam exam) {

        ExamOutputDTO.ExamOutputDTOBuilder builder = ExamOutputDTO.builder()
                .examId(exam.getExamId())
                .title(exam.getTitle())
                .description(exam.getDescription())
                .examDate(exam.getExamDate())
                .durationMinutes(exam.getDurationMinutes())
                .totalMarks(exam.getTotalMarks())
                .passingMarks(exam.getPassingMarks())
                .createdAt(exam.getCreatedAt());

        if (exam.getCourse() != null) {
            builder.courseId(exam.getCourse().getCourseId());
            builder.courseTitle(exam.getCourse().getTitle());
        }

        if (exam.getInstructor() != null) {
            builder.instructorId(exam.getInstructor().getInstructorId());
            if (exam.getInstructor().getUser() != null) {
                builder.instructorName(exam.getInstructor().getUser().getName());
            }
        }

        // Populate room info from the first room assigned to this exam (if any)
        if (exam.getExamId() != null) {
            List<ExamRoom> rooms = examRoomRepository.findByExam_ExamId(exam.getExamId());
            if (!rooms.isEmpty()) {
                ExamRoom room = rooms.get(0);
                builder.roomId(room.getRoomId());
                builder.roomName(room.getRoomName());
                builder.roomLocation(room.getLocation());
                builder.roomNumber(room.getRoomNumber());
            }
        }

        return builder.build();
    }
}
