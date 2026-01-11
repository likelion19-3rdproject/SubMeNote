package com.backend.comment.repository;

import com.backend.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findAllByPostIdOrderByCreatedAtDesc(Long postId, Pageable pageable);

    // 내가 쓴 댓글 조회 (최신순)
    Page<Comment> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}