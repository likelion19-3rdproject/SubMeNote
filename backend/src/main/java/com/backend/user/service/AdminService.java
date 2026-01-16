package com.backend.user.service;

import com.backend.user.dto.ApplicationProcessRequestDto;
import com.backend.user.dto.CreatorApplicationResponseDto;
import org.springframework.data.domain.Page;

public interface AdminService {
    Page<CreatorApplicationResponseDto> getPendingApplications(Long adminId, int page, int size);

    void processApplication(Long adminId, Long applicationId, ApplicationProcessRequestDto requestDto);
}
