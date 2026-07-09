package com.cts.service;

import org.springframework.web.multipart.MultipartFile;
import com.cts.dto.AssignmentInputDTO;
import com.cts.dto.AssignmentOutputDTO;

public interface AssignmentService {

    AssignmentOutputDTO publishAssignment(AssignmentInputDTO inputDTO, MultipartFile file);
}