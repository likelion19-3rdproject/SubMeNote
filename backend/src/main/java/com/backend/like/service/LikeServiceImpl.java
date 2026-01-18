package com.backend.like.service;


import com.backend.comment.repository.CommentRepository;

import com.backend.global.exception.common.BusinessException;
import com.backend.like.entity.Like;
import com.backend.like.entity.LikeTargetType;
import com.backend.like.repository.LikeRepository;
import com.backend.post.repository.PostRepository;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.backend.global.exception.CommentErrorCode.COMMENT_NOT_FOUND;
import static com.backend.global.exception.PostErrorCode.POST_NOT_FOUND;
import static com.backend.global.exception.UserErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Override
    public LikeToggleResult toggle(Long userId, LikeTargetType type, Long targetId) {

        validateTargetExists(type, targetId);

        // 2번 누르면 취소
        if (likeRepository.existsByUser_IdAndTargetTypeAndTargetId(userId, type, targetId)) {
            likeRepository.deleteByUser_IdAndTargetTypeAndTargetId(userId, type, targetId);
            long cnt = likeRepository.countByTargetTypeAndTargetId(type, targetId);
            return new LikeToggleResult(false, cnt);
        }

        // 1번 누르면 좋아요
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        try {
            likeRepository.save(Like.of(user, type, targetId));
        } catch (DataIntegrityViolationException e) {
            // 동시요청(더블클릭)으로 중복 insert 시 유니크 제약이 막음 -> 최종상태 조회로 반환
        }

        long cnt = likeRepository.countByTargetTypeAndTargetId(type, targetId);
        boolean liked = likeRepository.existsByUser_IdAndTargetTypeAndTargetId(userId, type, targetId);
        return new LikeToggleResult(liked, cnt);
    }

    @Override
    @Transactional(readOnly = true)
    public long count(LikeTargetType type, Long targetId) {
        return likeRepository.countByTargetTypeAndTargetId(type, targetId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean likedByMe(Long userId, LikeTargetType type, Long targetId) {
        if (userId == null) return false;
        return likeRepository.existsByUser_IdAndTargetTypeAndTargetId(userId, type, targetId);
    }

    private void validateTargetExists(LikeTargetType type, Long targetId) {
        switch (type) {
            case POST -> {
                if (!postRepository.existsById(targetId)) {
                    throw new BusinessException(POST_NOT_FOUND);
                }
            }
            case COMMENT -> {
                if (!commentRepository.existsById(targetId)) {
                    throw new BusinessException(COMMENT_NOT_FOUND);
                }
            }

        }
    }
}