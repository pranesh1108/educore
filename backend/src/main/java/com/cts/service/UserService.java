package com.cts.service;

import com.cts.dto.LoginDTO;
import com.cts.dto.LoginResponseDTO;
import com.cts.dto.RegistrationInputDTO;
import com.cts.dto.RegistrationOutputDTO;

public interface UserService {

    RegistrationOutputDTO addUser(RegistrationInputDTO registrationInputDTO);

    LoginResponseDTO userLogin(LoginDTO loginDTO);


}
