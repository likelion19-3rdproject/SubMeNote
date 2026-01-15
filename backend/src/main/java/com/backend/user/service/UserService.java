package com.backend.user.service;

import com.backend.user.dto.CreatorAccountRequestDto;
import com.backend.user.dto.CreatorApplicationRequestDto;
import com.backend.user.dto.CreatorApplicationResponseDto;
import com.backend.user.dto.CreatorResponseDto;
import org.springframework.data.domain.Page;

public interface UserService {
    Page<CreatorResponseDto> listAllCreators(int page, int size);

    void signout(Long userId);

    void registerAccount(Long userId, CreatorAccountRequestDto requestDto);

    void updateAccount(Long userId, CreatorAccountRequestDto requestDto);

    void applyForCreator(Long userId, CreatorApplicationRequestDto requestDto);

    CreatorApplicationResponseDto getMyApplication(Long userId);
}
