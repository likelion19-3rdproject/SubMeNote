package com.backend.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "creator_applications")
@Getter
@NoArgsConstructor
public class CreatorApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    public CreatorApplication(Long userId, String nickname) {
        this.userId = userId;
        this.nickname = nickname;
        this.status = ApplicationStatus.PENDING;
        this.appliedAt = LocalDateTime.now();
    }
}
