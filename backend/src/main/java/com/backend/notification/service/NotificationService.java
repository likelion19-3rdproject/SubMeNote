package com.backend.notification.service;

import com.backend.notification.dto.AnnouncementResponseDto;
import com.backend.notification.dto.NotificationReadResponseDto;
import com.backend.notification.dto.NotificationResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {

    Page<NotificationResponseDto> findMyNotifications(Long userId, Pageable pageable);


    NotificationResponseDto findNotification(Long userId, Long notificationId);

    void deleteNotification(Long userId, Long notificationId);

    NotificationReadResponseDto readNotifications(Long userId, List<Long> ids);


}
