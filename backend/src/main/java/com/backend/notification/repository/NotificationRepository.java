package com.backend.notification.repository;

import com.backend.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
    Page<Notification> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Modifying
    @Query("update Notification n set n.readAt = :now where n.user.id = :userId and n.id in :ids and n.readAt is null")
    int UpdateReadByUserAndIds(Long userId, List<Long> ids, LocalDateTime now);
}
