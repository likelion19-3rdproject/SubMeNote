package com.backend.user.service;

import com.backend.user.dto.CreatorApplicationResponseDto;
import com.backend.user.dto.UserResponseDto;

public interface UserService {

    UserResponseDto getMe(Long userId);

    void signout(Long userId);

    void applyForCreator(Long userId);

    CreatorApplicationResponseDto getMyApplication(Long userId);
}
