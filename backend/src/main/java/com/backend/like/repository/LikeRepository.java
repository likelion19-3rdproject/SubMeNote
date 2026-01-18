package com.backend.like.repository;

import com.backend.like.entity.Like;
import com.backend.like.entity.LikeTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {

    boolean existsByUser_IdAndTargetTypeAndTargetId(Long userId, LikeTargetType type, Long targetId);

    long countByTargetTypeAndTargetId(LikeTargetType type, Long targetId);

    void deleteByUser_IdAndTargetTypeAndTargetId(Long userId, LikeTargetType type, Long targetId);
}