package com.backend.like.service;

import com.backend.like.entity.LikeTargetType;

public interface LikeService {

    LikeToggleResult toggle(Long userId, LikeTargetType type, Long targetId);

    long count(LikeTargetType type, Long targetId);

    boolean likedByMe(Long userId, LikeTargetType type, Long targetId);

    record LikeToggleResult(boolean liked, long likeCount) {}
}