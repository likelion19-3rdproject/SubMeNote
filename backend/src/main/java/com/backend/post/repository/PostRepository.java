package com.backend.post.repository;

import com.backend.post.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface  PostRepository extends JpaRepository<Post, Long> {
}
