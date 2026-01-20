package com.backend.comment.service.impl;

import com.backend.comment.dto.CommentCreateRequestDto;
import com.backend.comment.dto.CommentResponseDto;
import com.backend.comment.dto.CommentUpdateRequestDto;
import com.backend.comment.entity.Comment;
import com.backend.comment.service.CommentService;
import com.backend.global.exception.domain.CommentErrorCode;
import com.backend.comment.repository.CommentRepository;
import com.backend.global.exception.common.BusinessException;
import com.backend.global.exception.domain.PostErrorCode;
import com.backend.notification.dto.NotificationContext;
import com.backend.notification.entity.NotificationType;
import com.backend.notification.entity.NotificationTargetType;
import com.backend.notification.service.NotificationCommand;
import com.backend.like.entity.LikeTargetType;
import com.backend.like.service.LikeService;
import com.backend.post.entity.Post;
import com.backend.post.repository.PostRepository;
import com.backend.global.validator.PostAccessValidator;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationCommand notificationCommand;
    private final LikeService likeService;
    private final PostAccessValidator postAccessValidator;

    /**
     * 댓글 생성
     * <br/>
     * 1. 게시글 존재 유무 확인
     * 2. 유저 존재 유무 확인
     * 3. 게시글 접근 권한 확인
     */
    @Override
    @Transactional
    public CommentResponseDto create(Long postId, Long userId, CommentCreateRequestDto request) {

        // 게시글이 존재하는지 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(PostErrorCode.POST_NOT_FOUND));

        // 작성자(유저)가 존재하는지 확인
        // 로그인한 사용자 정보는 컨트롤러에서 @AuthenticationPrincipal로 전달됨
        // 여기서는 전달받은 userId가 실제 사용자로 존재하는지 확인
        User user = userRepository.findByIdOrThrow(userId);

        // 게시글 접근권한 체크
        // 게시글 접근 권한이 있어야 댓글도 쓸 수 있음 -> 권한 체크 로직 호출
        postAccessValidator.validatePostAccess(post, userId);

        // 부모 댓글 처리 로직
        Comment parent = null;

        if (request.parentId() != null) {
            parent = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND));

            // 검증1 : 부모 댓글이 다른 게시글에 있으면 안됨
            if (!parent.getPost().getId().equals(postId)) {
                throw new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND); //혹은 적절한 에러코드
            }

            //검증 2: 대댓글의 대댓글 금지 (여기에 추가)
            if (parent.getParent() != null) {
                throw new BusinessException(CommentErrorCode.REPLY_DEPTH_EXCEEDED);
            }
        }

        Comment comment = Comment.create(
                user,
                post,
                parent,
                request.content()
        );

        Comment saved = commentRepository.save(comment);

        notificationCommand.createNotification(
                post.getUser().getId(),
                NotificationType.COMMENT_CREATED,
                NotificationTargetType.POST,
                post.getId(),
                NotificationContext.forComment(user.getNickname())
        );

        return CommentResponseDto.from(saved, postId);
    }

    /**
     * 댓글 수정
     */
    @Override
    @Transactional
    public CommentResponseDto update(Long commentId, Long userId, CommentUpdateRequestDto request) {

        // 수정할 댓글이 존재하는지 확인
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND));

        // 댓글작성자만 수정 가능
        if (!userId.equals(comment.getUser().getId())) {
            throw new BusinessException(CommentErrorCode.COMMENT_FORBIDDEN);
        }

        comment.update(request.content());

        Long postId = comment.getPost().getId();

        return CommentResponseDto.from(comment, postId);
    }

    /**
     * 댓글 삭제
     */
    @Override
    @Transactional
    public void delete(Long commentId, Long userId) {

        // 댓글 존재 여부 확인
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND));

        // 댓글작성자만 삭제 가능
        if (!userId.equals(comment.getUser().getId())) {
            throw new BusinessException(CommentErrorCode.COMMENT_FORBIDDEN);
        }

        commentRepository.delete(comment); //하드삭제
    }

    /**
     * 댓글 조회
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getComments(Long postId, Long currentUserId, Pageable pageable) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(PostErrorCode.POST_NOT_FOUND));

        postAccessValidator.validatePostAccess(post, currentUserId);

        Page<Comment> rootsPage = commentRepository.findRootsWithUser(postId, pageable);
        List<Comment> roots = rootsPage.getContent();

        // 루트가 없으면 끝, 그대로 빈 페이지 리턴
        if (roots.isEmpty()) {
            return rootsPage.map(c ->
                    toDtoFlat(c, postId, currentUserId, Map.of(), Set.of(), List.of())
            );
        }

        List<Long> rootIds = roots.stream().map(Comment::getId).toList();
        List<Comment> children = commentRepository.findChildrenWithUser(rootIds);

        // 댓글/대댓글 전체 id 모아서 좋아요 배치 조회
        List<Long> allIds = new java.util.ArrayList<Long>(roots.size() + children.size());
        roots.forEach(c -> allIds.add(c.getId()));
        children.forEach(c -> allIds.add(c.getId()));

        Map<Long, Long> likeCountMap = likeService.countMap(LikeTargetType.COMMENT, allIds);
        java.util.Set<Long> likedSet = likeService.likedSet(currentUserId, LikeTargetType.COMMENT, allIds);

        // parentId -> children dto 리스트 매핑
        Map<Long, List<CommentResponseDto>> childrenByParentId = new java.util.HashMap<>();
        for (Comment child : children) {
            Long parentId = child.getParent().getId();
            childrenByParentId.computeIfAbsent(parentId, k -> new java.util.ArrayList<>())
                    .add(toDtoFlat(child, postId, currentUserId, likeCountMap, likedSet, List.of()));
        }

        // 루트 dto 생성하면서 children 붙이기
        return rootsPage.map(root -> {
            List<CommentResponseDto> childDtos =
                    childrenByParentId.getOrDefault(root.getId(), List.of());
            return toDtoFlat(root, postId, currentUserId, likeCountMap, likedSet, childDtos);
        });
    }

    /**
     * 내가 작성한 댓글 조회
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getMyComments(Long userId, Pageable pageable) {

        return commentRepository
                .findAllByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(c -> CommentResponseDto.from(c, c.getPost().getId()));
    }

    // like
    // children을 인자로 받는 “평면 DTO 생성” (재귀 금지)
    private CommentResponseDto toDtoFlat(
            Comment comment,
            Long postId,
            Long currentUserId,
            Map<Long, Long> likeCountMap,
            Set<Long> likedSet,
            List<CommentResponseDto> children
    ) {

        long likeCount = likeCountMap.getOrDefault(comment.getId(), 0L);
        boolean likedByMe = currentUserId != null && likedSet.contains(comment.getId());

        return new CommentResponseDto(
                comment.getId(),
                comment.getUser().getId(),
                comment.getUser().getNickname(),
                comment.getContent(),
                comment.getStatus(),
                postId,
                comment.getParent() != null ? comment.getParent().getId() : null,
                children,
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                likeCount,
                likedByMe
        );
    }
}