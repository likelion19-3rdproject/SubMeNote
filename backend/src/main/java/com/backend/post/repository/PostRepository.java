package com.backend.post.repository;

import com.backend.post.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface  PostRepository extends JpaRepository<Post, Long> {
    // 생성일 내림차순(최신순) 조회
    List<Post> findAllByOrderByCreatedAtDesc();
}
