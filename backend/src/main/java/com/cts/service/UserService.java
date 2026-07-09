package com.cts.service;

import com.cts.dto.LoginDTO;
import com.cts.dto.LoginResponseDTO;
import com.cts.dto.RegistrationInputDTO;
import com.cts.dto.RegistrationOutputDTO;
import com.cts.dto.RoleIdentityDTO;

public interface UserService {

    RegistrationOutputDTO addUser(RegistrationInputDTO registrationInputDTO);

    LoginResponseDTO userLogin(LoginDTO loginDTO);

    // Returns roleId + email for /api/v1/{role}/{email}
    // role can be: registrar, student, instructor, exam-coordinator
    RoleIdentityDTO getRoleIdentity(String role, String email);
}
