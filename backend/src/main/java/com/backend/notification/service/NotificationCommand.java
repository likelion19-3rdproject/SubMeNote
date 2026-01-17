package com.backend.notification.service;

import com.backend.global.exception.UserErrorCode;
import com.backend.global.exception.common.BusinessException;
import com.backend.notification.dto.NotificationContext;
import com.backend.notification.entity.Notification;
import com.backend.notification.entity.NotificationType;
import com.backend.notification.entity.NotificationTargetType;
import com.backend.notification.repository.NotificationRepository;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class NotificationCommand {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;


    @Transactional
    public void createNotification(
            Long receiverUserId,
            NotificationType type,
            NotificationTargetType notificationTargetType,
            Long targetId,
            NotificationContext context
    ) {
        User receiver = userRepository.findById(receiverUserId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        NotificationType.Message msg = type.createMessage(context);

        Notification notification = Notification.create(
                receiver,
                type,
                notificationTargetType,
                targetId,
                msg.title(),
                msg.body()
        );
        notificationRepository.save(notification);
    }
}