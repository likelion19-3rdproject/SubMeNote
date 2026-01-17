package com.backend.notification.entity;

import com.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @Column
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @Column
    @Enumerated(EnumType.STRING)
    private NotificationTargetType notificationTargetType;

    @Column
    private Long targetId;

    @Column
    private String title;

    @Column
    private String message;

    @Column
    @CreatedDate
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime readAt;

    private Notification(User user, NotificationType notificationType, NotificationTargetType notificationTargetType, Long targetId, String title, String message) {
        this.user = user;
        this.notificationType = notificationType;
        this.notificationTargetType = notificationTargetType;
        this.targetId = targetId;
        this.title = title;
        this.message = message;
    }

    public static Notification create(User user, NotificationType notificationType,
                                      NotificationTargetType notificationTargetType, Long targetId, String title, String  message){
        return new Notification(user,notificationType, notificationTargetType,targetId,title,message);
    }

    public void readNotification(LocalDateTime now) {
        if (this.readAt == null) {
            this.readAt = now;
        }
    }




}
