package com.cts.mapper;

import org.springframework.stereotype.Component;

import com.cts.dto.UserResponseDTO;
import com.cts.entity.User;

@Component
public class UserMapper {

	public UserResponseDTO toUserResponseDTO(User user) {
        return UserResponseDTO.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
