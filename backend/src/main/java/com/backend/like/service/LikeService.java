package com.backend.like.service;

import com.backend.like.entity.LikeTargetType;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LikeService {

    LikeToggleResult toggle(Long userId, LikeTargetType type, Long targetId);

    long count(LikeTargetType type, Long targetId);

    boolean likedByMe(Long userId, LikeTargetType type, Long targetId);

    record LikeToggleResult(boolean liked, long likeCount) {}

    Map<Long, Long> countMap(LikeTargetType type, List<Long> targetId);

    Set<Long> likedSet(Long userId, LikeTargetType type, List<Long> targetId);
}