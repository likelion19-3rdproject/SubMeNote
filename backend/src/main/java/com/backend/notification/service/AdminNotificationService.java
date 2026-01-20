package com.backend.notification.service;

import com.backend.notification.dto.AnnouncementResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminNotificationService {

    void announceToAll(Long adminId, String message);

    Page<AnnouncementResponseDto> getAnnouncementList(Long userId, Pageable pageable);
}
