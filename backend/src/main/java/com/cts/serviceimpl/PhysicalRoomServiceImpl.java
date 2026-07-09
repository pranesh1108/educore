package com.cts.serviceimpl;

import com.cts.annotation.AuditEvent;
import com.cts.dto.PhysicalRoomInputDTO;
import com.cts.dto.PhysicalRoomOutputDTO;
import com.cts.entity.PhysicalRoom;
import com.cts.exception.BusinessException;
import com.cts.repository.PhysicalRoomRepository;
import com.cts.service.PhysicalRoomService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PhysicalRoomServiceImpl implements PhysicalRoomService {

    private final PhysicalRoomRepository physicalRoomRepository;

    @Override
    @Transactional
    @AuditEvent(eventName = "PHYSICAL_ROOM_CREATED", eventType = "CREATE",
            eventMessage = "A new physical room was created")
    public PhysicalRoomOutputDTO createRoom(PhysicalRoomInputDTO inputDTO) {
        if (physicalRoomRepository.existsByRoomNameIgnoreCase(inputDTO.getRoomName())) {
            throw new BusinessException(
                    "A room with name '" + inputDTO.getRoomName() + "' already exists.");
        }

        PhysicalRoom room = PhysicalRoom.builder()
                .roomName(inputDTO.getRoomName())
                .location(inputDTO.getLocation())
                .capacity(inputDTO.getCapacity())
                .status("AVAILABLE")
                .assignedExamId(null)
                .createdAt(LocalDateTime.now())
                .build();

        return toOutputDTO(physicalRoomRepository.save(room));
    }

    @Override
    @AuditEvent(eventName = "PHYSICAL_ROOMS_FETCHED", eventType = "READ",
            eventMessage = "Physical rooms were fetched")
    public List<PhysicalRoomOutputDTO> getAllRooms(String status) {
        List<PhysicalRoom> rooms;
        if (status != null && !status.isBlank()) {
            String normalized = status.trim().toUpperCase();
            if (!normalized.equals("AVAILABLE") && !normalized.equals("OCCUPIED")) {
                throw new BusinessException(
                        "Invalid status '" + status + "'. Allowed values: AVAILABLE, OCCUPIED");
            }
            rooms = physicalRoomRepository.findByStatus(normalized);
        } else {
            rooms = physicalRoomRepository.findAll();
        }
        return rooms.stream().map(this::toOutputDTO).collect(Collectors.toList());
    }

    private PhysicalRoomOutputDTO toOutputDTO(PhysicalRoom room) {
        return PhysicalRoomOutputDTO.builder()
                .roomId(room.getRoomId())
                .roomName(room.getRoomName())
                .location(room.getLocation())
                .capacity(room.getCapacity())
                .status(room.getStatus())
                .assignedExamId(room.getAssignedExamId())
                .assignedFrom(room.getAssignedFrom())
                .assignedUntil(room.getAssignedUntil())
                .createdAt(room.getCreatedAt())
                .build();
    }
}