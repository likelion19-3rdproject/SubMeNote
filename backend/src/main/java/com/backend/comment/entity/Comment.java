package com.backend.comment.entity;

import com.backend.post.entity.Post;
import com.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false, length = 1000)
    private String content;

    //부모 댓글
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    //자식 댓글 리스트, 하드삭제(부모댓글 삭제시 자식도 함께 삭제)
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> children = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private CommentReportStatus status ;


    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 댓글 생성
    public static Comment create(User user, Post post, Comment parent ,String content) {
        Comment comment = new Comment();
        comment.user = user;
        comment.post = post;
        comment.parent = parent;
        comment.content = content;
        comment.status= CommentReportStatus.NORMAL;
        return comment;
    }

    // 댓글 수정
    public void update(String content) {
        this.content = content;
    }

    public void hiddenComment(){
        this.status = CommentReportStatus.REPORT;
    }
    public void restoreComment(){
        this.status = CommentReportStatus.NORMAL;
    }
}