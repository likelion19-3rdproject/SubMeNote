package com.backend.user.dto;

import com.backend.role.entity.RoleEnum;
import com.backend.user.entity.User;

import java.util.List;

public record UserResponseDto(
        Long id,
        String email,
        String nickname,
        List<RoleEnum> roles
) {
    public static UserResponseDto from(User user) {
        List<RoleEnum> roles = user.getRole().stream()
                .map(role -> role.getRole())
                .toList();
        
        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                roles
        );
    }
}
