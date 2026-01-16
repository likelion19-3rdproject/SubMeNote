package com.backend.user.service;

import com.backend.user.dto.*;
import org.springframework.data.domain.Page;

public interface UserService {
    Page<CreatorResponseDto> listAllCreators(int page, int size);

    UserResponseDto getMe(Long userId);

    void signout(Long userId);

    void registerAccount(Long userId, AccountRequestDto requestDto);

    void updateAccount(Long userId, AccountRequestDto requestDto);

    void applyForCreator(Long userId);

    CreatorApplicationResponseDto getMyApplication(Long userId);

    AccountResponseDto getAccount(Long userId);
}
