package com.backend.report.entity;

import com.backend.comment.entity.Comment;
import com.backend.post.entity.Post;
import com.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "reports",
        uniqueConstraints = {
                @UniqueConstraint(name="uk_reporter_post", columnNames={"reporter_id","post_id"}),
                @UniqueConstraint(name="uk_reporter_comment", columnNames={"reporter_id","comment_id"})
        }
)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name ="user_id",nullable = false)
    private User user;  //신고자

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "post_id", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "comment_id", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Comment comment;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportType type;

    @Column(nullable = true)
    private String customReason;

    @Column(nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    private Report(
            User user,
            Post post,
            Comment comment,
            ReportType type,
            String customReason
    ) {
        this.user = user;
        this.post = post;
        this.comment = comment;
        this.type = type;
        this.customReason = customReason;
    }

    public static Report of(
            User user,
            Post post,
            Comment comment,
            ReportType type,
            String customReason
    ) {
        return new Report(
                user,
                post,
                comment,
                type,
                customReason
        );
    }
}
