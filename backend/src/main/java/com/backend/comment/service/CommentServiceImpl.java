package com.backend.comment.service;

import com.backend.comment.dto.CommentCreateRequestDto;
import com.backend.comment.dto.CommentResponseDto;
import com.backend.comment.dto.CommentUpdateRequestDto;
import com.backend.comment.entity.Comment;
import com.backend.global.exception.CommentErrorCode;
import com.backend.comment.repository.CommentRepository;
import com.backend.global.exception.UserErrorCode;
import com.backend.global.exception.common.BusinessException;
import com.backend.global.exception.PostErrorCode;
import com.backend.notification.dto.NotificationContext;
import com.backend.notification.entity.NotificationType;
import com.backend.notification.entity.NotificationTargetType;
import com.backend.notification.service.NotificationCommand;
import com.backend.like.entity.LikeTargetType;
import com.backend.like.service.LikeService;
import com.backend.post.entity.Post;
import com.backend.post.entity.PostVisibility;
import com.backend.post.repository.PostRepository;
import com.backend.role.entity.RoleEnum;
import com.backend.subscribe.entity.Subscribe;
import com.backend.subscribe.entity.SubscribeType;
import com.backend.subscribe.repository.SubscribeRepository;
import com.backend.subscribe.service.SubscribeService;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final SubscribeRepository subscribeRepository;
    private final NotificationCommand notificationCommand;
    private final LikeService likeService;
    private final SubscribeService subscribeService;


    //댓글 생성(등록)
    @Override
    public CommentResponseDto create(Long postId, Long userId, CommentCreateRequestDto request) {
        //댓글을 달 게시글이 존재하는지 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(PostErrorCode.POST_NOT_FOUND));

        //작성자(유저)가 존재하는지 확인
        // 로그인한 사용자 정보는 컨트롤러에서 @AuthenticationPrincipal로 전달됨
        // 여기서는 전달받은 userId가 실제 사용자로 존재하는지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        //게시글 접근권한 체크
        //게시글 접근 권한이 있어야 댓글도 쓸 수 있음 -> 권한 체크 로직 호출
        validatePostAccess(post, userId);

        //부모 댓글 처리 로직
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

        Comment comment = Comment.create(user, post, parent, request.content());
        Comment saved = commentRepository.save(comment);
        notificationCommand.createNotification(post.getUser().getId(), NotificationType.COMMENT_CREATED, NotificationTargetType.POST, post.getId(), NotificationContext.forComment(user.getNickname()));

        return CommentResponseDto.from(saved, postId);
    }

    //댓글 수정
    @Override
    public CommentResponseDto update(Long commentId, Long userId, CommentUpdateRequestDto request) {
        //수정할 댓글이 존재하는지 확인
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND));

        //댓글작성자만 수정 가능
        if (!userId.equals(comment.getUser().getId())) {
            throw new BusinessException(CommentErrorCode.COMMENT_FORBIDDEN);
        }

        comment.update(request.content());
        
        Long postId = comment.getPost().getId();
        return CommentResponseDto.from(comment, postId);
    }

    //댓글 삭제
    @Override
    public void delete(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND));

        //댓글작성자만 삭제 가능
        if (!userId.equals(comment.getUser().getId())) {
            throw new BusinessException(CommentErrorCode.COMMENT_FORBIDDEN);
        }

        commentRepository.delete(comment); //하드삭제
    }

    //좋아요 구현시 n+1이 터지는 이유 : 부모 n + 자식 m이면 쿼리가 미친듯이 증가
    //재귀로 무한 depth까지 dto를 만드는게 아니라 대댓글 1단계까지만 만들게 변경함
    //조회 매핑이 단순해지고 좋아요도 부모+자식을 한번에 처리하기 쉬워짐
    //댓글 조회, 일단 1-depth 로
    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getComments(Long postId, Long currentUserId, Pageable pageable) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(PostErrorCode.POST_NOT_FOUND));

        validatePostAccess(post, currentUserId);

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

    //내가 작성한 댓글 조회
    @Override
    public Page<CommentResponseDto> getMyComments(Long userId, Pageable pageable) {
        return commentRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(c -> CommentResponseDto.from(c, c.getPost().getId()));
    }

    // =================================================================
    // 내부 검증 로직 (PostServiceImpl과 동일한 로직 사용)
    // =================================================================

    // 게시글 접근 권한 확인 (댓글 작성/조회 시 사용)
    private void validatePostAccess(Post post, Long currentUserId) {
        // 1. 로그인 체크
        if (currentUserId == null) {
            throw new BusinessException(PostErrorCode.LOGIN_REQUIRED);
        }

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        // 2. 작성자 본인은 프리패스
        if (post.getUser().getId().equals(currentUserId)) {
            return;
        }

        // 2-1. 어드민은 모든 포스트에 댓글 작성 가능
        if (user.hasRole(RoleEnum.ROLE_ADMIN)) {
            return;
        }

        // 3. 구독 확인
        Subscribe subscribe = subscribeService.validateSubscription(post.getUser().getId(), currentUserId);

        // 4. 유료 글 추가 체크
        if (post.getVisibility() == PostVisibility.SUBSCRIBERS_ONLY) {
            if (subscribe.getType() != SubscribeType.PAID) {
                throw new BusinessException(PostErrorCode.PAID_SUBSCRIPTION_REQUIRED);
            }
        }
    }

    // 구독 여부 및 만료 확인
    private Subscribe validateSubscription(Long creatorId, Long subscriberId) {
        Subscribe subscribe = subscribeRepository.findByUser_IdAndCreator_Id(subscriberId, creatorId)
                .orElseThrow(() -> new BusinessException(PostErrorCode.SUBSCRIPTION_REQUIRED));

        if (subscribe.getExpiredAt() != null && subscribe.getExpiredAt().isBefore(LocalDate.now())) {
            throw new BusinessException(PostErrorCode.SUBSCRIPTION_REQUIRED);
        }
        return subscribe;
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