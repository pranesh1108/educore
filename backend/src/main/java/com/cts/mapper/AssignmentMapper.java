package com.cts.mapper;

import org.springframework.stereotype.Component;
import com.cts.dto.AssignmentFileOutputDTO;
import com.cts.dto.AssignmentOutputDTO;
import com.cts.entity.Assignment;
import com.cts.entity.AssignmentFile;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AssignmentMapper {

    public AssignmentOutputDTO toAssignmentOutputDTO(Assignment a, List<AssignmentFile> files) {
        List<AssignmentFileOutputDTO> fileDTOs = null;
        if (files != null) {
            fileDTOs = files.stream()
                    .map(this::toAssignmentFileOutputDTO)
                    .collect(Collectors.toList());
        }
        return AssignmentOutputDTO.builder()
                .assignmentId(a.getAssignmentId())
                .title(a.getTitle())
                .instructions(a.getInstructions())
                .totalMarks(a.getTotalMarks())
                .publishedAt(a.getPublishedAt())
                .dueDate(a.getDueDate())
                .courseId(a.getCourse().getCourseId())
                .courseTitle(a.getCourse().getTitle())
                .files(fileDTOs)
                .build();
    }

    public AssignmentFileOutputDTO toAssignmentFileOutputDTO(AssignmentFile f) {
        return AssignmentFileOutputDTO.builder()
                .fileId(f.getFileId())
                .assignmentId(f.getAssignment().getAssignmentId())
                .assignmentTitle(f.getAssignment().getTitle())
                .fileName(f.getFileName())
                .uploadedAt(f.getUploadedAt())
                .build();
    }
}
