package com.cts.serviceimpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.cts.exception.AccessDeniedException;
import com.cts.repository.*;
import com.cts.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cts.annotation.AuditEvent;
import com.cts.dto.ExamRoomInputDTO;
import com.cts.dto.ExamRoomOutputDTO;
import com.cts.entity.Exam;
import com.cts.entity.ExamRoom;
import com.cts.entity.ExamRoomAllocation;
import com.cts.entity.PhysicalRoom;
import com.cts.entity.Student;
import com.cts.exception.BusinessException;
import com.cts.exception.ExamNotFoundException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.mapper.ExamRoomMapper;
import com.cts.service.ExamRoomService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ExamRoomServiceImpl implements ExamRoomService {

    private final ExamRepository examRepository;
    private final ExamRoomRepository examRoomRepository;
    private final ExamRoomAllocationRepository allocationRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final PhysicalRoomRepository physicalRoomRepository;
    private final ExamRoomMapper examRoomMapper;
    private final ExamCoordinatorRepository examCoordinatorRepository;

    private void verifyCoordinatorContext() {
        String loggedInEmail = SecurityUtils.getLoggedInEmail();
        examCoordinatorRepository.findByUser_Email(loggedInEmail)
                .orElseThrow(() -> new AccessDeniedException(
                        "Access Denied: Logged-in credentials do not belong to a valid Exam Coordinator profile."));
    }

    @Override
    @Transactional
    @AuditEvent(
            eventName    = "EXAM_ROOM_ASSIGNED",
            eventType    = "CREATE",
            eventMessage = "A physical room was assigned to an exam and students were auto-allocated"
    )
    public ExamRoomOutputDTO createAndAllocate(ExamRoomInputDTO inputDTO) {
        verifyCoordinatorContext();

        Exam exam = examRepository.findById(inputDTO.getExamId())
                .orElseThrow(() -> new ExamNotFoundException(
                        "Exam not found with id: " + inputDTO.getExamId()));

        if (exam.getExamDate() == null) {
            throw new BusinessException(
                    "Exam id " + exam.getExamId() + " has no exam date set. "
                    + "Please update the exam with a date before assigning a room.");
        }
        if (exam.getDurationMinutes() == null || exam.getDurationMinutes() <= 0) {
            throw new BusinessException(
                    "Exam id " + exam.getExamId() + " has no valid duration set. "
                    + "Please update the exam with a duration before assigning a room.");
        }

        LocalDateTime examStart = exam.getExamDate();
        LocalDateTime examEnd   = examStart.plusMinutes(exam.getDurationMinutes());

        PhysicalRoom physicalRoom = physicalRoomRepository.findById(inputDTO.getPhysicalRoomId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Physical room not found with id: " + inputDTO.getPhysicalRoomId()
                        + ". Create it first using POST /api/v1/exam-coordinator/rooms."));

        boolean timeConflict = physicalRoomRepository.hasTimeOverlap(
                physicalRoom.getRoomId(), examStart, examEnd, inputDTO.getExamId());

        if (timeConflict) {
            String occupiedWindow = physicalRoom.getAssignedFrom() + " to " + physicalRoom.getAssignedUntil();
            throw new BusinessException(
                    "Room '" + physicalRoom.getRoomName()
                    + "' is not available from " + examStart + " to " + examEnd
                    + ". It is already booked during " + occupiedWindow
                    + " for exam id: " + physicalRoom.getAssignedExamId()
                    + ". Please choose a different room or reschedule the exam.");
        }

        Integer roomNumberVal = inputDTO.getRoomNumber();
        if (roomNumberVal == null || roomNumberVal <= 0) {
            roomNumberVal = 1;
        }
        while (examRoomRepository.existsByExam_ExamIdAndRoomNumber(exam.getExamId(), roomNumberVal)) {
            roomNumberVal++;
        }

        Long courseId = exam.getCourse().getCourseId();
        List<Student> enrolledStudents = enrollmentRepository
                .findByCourse_CourseId(courseId)
                .stream()
                .map(e -> e.getStudent())
                .collect(Collectors.toList());

        if (enrolledStudents.isEmpty()) {
            throw new BusinessException(
                    "No students are enrolled in the course for this exam.");
        }

        List<Long> alreadyAllocatedIds = allocationRepository
                .findAllocatedStudentIdsByExamId(inputDTO.getExamId());

        List<Student> unallocatedStudents = enrolledStudents.stream()
                .filter(s -> !alreadyAllocatedIds.contains(s.getStudentId()))
                .collect(Collectors.toList());

        if (unallocatedStudents.isEmpty()) {
            throw new BusinessException(
                    "All " + enrolledStudents.size()
                    + " enrolled students have already been allocated to rooms "
                    + "for exam id: " + inputDTO.getExamId()
                    + ". No students left to allocate.");
        }

        int capacity = physicalRoom.getCapacity();
        List<Student> batch = unallocatedStudents.stream()
                .limit(capacity)
                .collect(Collectors.toList());

        ExamRoom room = ExamRoom.builder()
                .roomName(physicalRoom.getRoomName())
                .location(physicalRoom.getLocation())
                .capacity(capacity)
                .exam(exam)
                .roomNumber(roomNumberVal)
                .createdAt(LocalDateTime.now())
                .build();

        ExamRoom savedRoom = examRoomRepository.save(room);

        List<ExamRoomAllocation> allocations = batch.stream()
                .map(student -> ExamRoomAllocation.builder()
                        .examRoom(savedRoom)
                        .student(student)
                        .exam(exam)
                        .allocatedAt(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());

        allocationRepository.saveAll(allocations);

        physicalRoom.setStatus("OCCUPIED");
        physicalRoom.setAssignedExamId(exam.getExamId());
        physicalRoom.setAssignedFrom(examStart);
        physicalRoom.setAssignedUntil(examEnd);
        physicalRoomRepository.save(physicalRoom);

        return examRoomMapper.toOutputDTO(savedRoom, allocations);
    }


    @Override
    @AuditEvent(
            eventName    = "EXAM_ROOMS_FETCHED",
            eventType    = "READ",
            eventMessage = "All rooms for an exam were fetched"
    )
    public List<ExamRoomOutputDTO> getRoomsForExam(Long examId) {
        verifyCoordinatorContext();
        examRepository.findById(examId)
                .orElseThrow(() -> new ExamNotFoundException(
                        "Exam not found with id: " + examId));

        return examRoomRepository.findByExam_ExamId(examId)
                .stream()
                .map(room -> {
                    List<ExamRoomAllocation> allocations =
                            allocationRepository.findByExamRoom_RoomId(room.getRoomId());
                    return examRoomMapper.toOutputDTO(room, allocations);
                })
                .collect(Collectors.toList());
    }
}
