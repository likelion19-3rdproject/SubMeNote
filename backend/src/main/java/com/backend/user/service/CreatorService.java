package com.backend.user.service;

import com.backend.user.dto.AccountRequestDto;
import com.backend.user.dto.AccountResponseDto;

public interface CreatorService {

    void registerAccount(Long userId, AccountRequestDto requestDto);

    AccountResponseDto getAccount(Long userId);

    void updateAccount(Long userId, AccountRequestDto requestDto);
}
