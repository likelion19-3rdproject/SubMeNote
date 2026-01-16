package com.backend.post.repository;

import com.backend.post.entity.Post;
import com.backend.post.entity.PostReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 1. 전체 조회
    Page<Post> findAll(Pageable pageable);
    // 2. 내 게시글 조회
    Page<Post> findAllByUserId(Long userId, Pageable pageable);

    // 3. 내가 구독한 크리에이터들의 글 목록 조회 (SQL의 IN 절 사용)
    // select * from posts where user_id in (1, 2, 5, ...)
    Page<Post> findAllByUserIdIn(List<Long> userIds, Pageable pageable);
    Page<Post> findByStatus(PostReportStatus status, Pageable pageable);

}
