package com.cts.serviceimpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.cts.dto.PhysicalRoomInputDTO;
import com.cts.dto.PhysicalRoomOutputDTO;
import com.cts.entity.PhysicalRoom;
import com.cts.repository.PhysicalRoomRepository;

public class PhysicalRoomServiceImplTest {

    @Mock private PhysicalRoomRepository physicalRoomRepository;

    @InjectMocks
    private PhysicalRoomServiceImpl physicalRoomService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createRoom_success() {
        PhysicalRoomInputDTO input = PhysicalRoomInputDTO.builder().roomName("Hall A").capacity(50).build();
        PhysicalRoom room = PhysicalRoom.builder().roomId(1L).roomName("Hall A").capacity(50).status("AVAILABLE").build();

        when(physicalRoomRepository.existsByRoomNameIgnoreCase("Hall A")).thenReturn(false);
        when(physicalRoomRepository.save(any(PhysicalRoom.class))).thenReturn(room);

        PhysicalRoomOutputDTO result = physicalRoomService.createRoom(input);

        assertNotNull(result);
        assertEquals("Hall A", result.getRoomName());
        assertEquals(50, result.getCapacity());
    }

    @Test
    void getAllRooms_success() {
        PhysicalRoom room = PhysicalRoom.builder().roomId(1L).roomName("Hall A").status("AVAILABLE").build();
        when(physicalRoomRepository.findAll()).thenReturn(List.of(room));

        List<PhysicalRoomOutputDTO> results = physicalRoomService.getAllRooms(null);

        assertNotNull(results);
        assertEquals(1, results.size());
    }
}