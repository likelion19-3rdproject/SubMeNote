package com.backend.post.repository;

import com.backend.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 1. 전체 조회
    Page<Post> findAll(Pageable pageable);
    // 2. 내 게시글 조회
    Page<Post> findAllByUserId(Long userId, Pageable pageable);
}
