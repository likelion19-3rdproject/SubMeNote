package com.backend.comment.repository;

import com.backend.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByPostIdOrderByCreatedAtDesc(Long postId);

    // 내가 쓴 댓글 조회 (최신순)
    List<Comment> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}