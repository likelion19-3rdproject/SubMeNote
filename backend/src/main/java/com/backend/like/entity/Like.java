package com.backend.like.entity;

import com.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "likes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_like_user_target",
                columnNames = {"user_id", "target_type", "target_id"}
        ),
        indexes = {
                @Index(name = "idx_like_target", columnList = "target_type, target_id"),
                @Index(name = "idx_like_user", columnList = "user_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 누른 사람
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 어디에 눌렀는지 (POST/COMMENT)
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private LikeTargetType targetType;

    // 대상 id (post_id or comment_id or reply_id)
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    private Like(User user, LikeTargetType targetType, Long targetId) {
        this.user = user;
        this.targetType = targetType;
        this.targetId = targetId;
    }

    public static Like of(User user, LikeTargetType targetType, Long targetId) {
        return new Like(user, targetType, targetId);
    }
}