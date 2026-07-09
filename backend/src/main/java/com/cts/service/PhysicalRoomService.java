package com.cts.service;

import com.cts.dto.PhysicalRoomInputDTO;
import com.cts.dto.PhysicalRoomOutputDTO;
import java.util.List;

public interface PhysicalRoomService {

    PhysicalRoomOutputDTO createRoom(PhysicalRoomInputDTO inputDTO);

    List<PhysicalRoomOutputDTO> getAllRooms(String status);
}