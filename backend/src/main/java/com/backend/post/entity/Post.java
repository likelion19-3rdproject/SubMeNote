package com.backend.post.entity;

import com.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob //게시글 내용 길이 늘림
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostVisibility visibility;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostReportStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Post(
            String title,
            String content,
            PostVisibility visibility,
            User user
    ) {
        this.title = title;
        this.content = content;
        this.visibility = visibility;
        this.user = user;
        this.status = PostReportStatus.NORMAL;
    }

    public static Post create(
            String title,
            String content,
            PostVisibility visibility,
            User user
    ) {
        return new Post(
                title,
                content,
                visibility,
                user
        );
    }

    public void update(
            String title,
            String content,
            PostVisibility visibility
    ) {
        if (title != null) this.title = title;
        if (content != null) this.content = content;
        if (visibility != null) this.visibility = visibility;
    }

    public void hiddenPost(){
        this.status = PostReportStatus.REPORT;
    }
    public void restorePost(){
        this.status = PostReportStatus.NORMAL;
    }
}
