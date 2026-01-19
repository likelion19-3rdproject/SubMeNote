package com.backend.user.service;

import com.backend.user.dto.ApplicationProcessRequestDto;
import com.backend.user.dto.CreatorApplicationResponseDto;
import com.backend.user.dto.CreatorResponseDto;
import com.backend.user.dto.UserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {
    Page<CreatorApplicationResponseDto> getPendingApplications(Long adminId, Pageable pageable);

    void processApplication(Long adminId, Long applicationId, ApplicationProcessRequestDto requestDto);

    Long getCreatorCount(Long userId);

    Page<CreatorResponseDto> getCreatorList(Long userId, Pageable pageable);

    Long getUserCount(Long userId);

    Page<UserResponseDto> getUserList(Long userId, Pageable pageable);
}
