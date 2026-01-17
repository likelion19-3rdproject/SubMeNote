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
import com.backend.post.entity.Post;
import com.backend.post.entity.PostVisibility;
import com.backend.post.repository.PostRepository;
import com.backend.subscribe.entity.Subscribe;
import com.backend.subscribe.entity.SubscribeType;
import com.backend.subscribe.repository.SubscribeRepository;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;


@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final SubscribeRepository subscribeRepository;
    private final NotificationCommand notificationCommand;

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

        Comment comment = Comment.create(user, post, request.content());
        Comment saved = commentRepository.save(comment);
        notificationCommand.createNotification(userId, NotificationType.COMMENT_CREATED, NotificationTargetType.COMMENT,saved.getId(),NotificationContext.forComment(user.getNickname()));

        return CommentResponseDto.from(saved);
    }

    //댓글 수정
    @Override
    public CommentResponseDto update(Long commentId, Long userId, CommentUpdateRequestDto request) {
        //수정할 댓글이 존재하는지 확인
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND));

        //댓글작성자만 수정 가능
        if(!userId.equals(comment.getUser().getId())){
            throw new BusinessException(CommentErrorCode.COMMENT_FORBIDDEN);
        }

        comment.update(request.content());

        return CommentResponseDto.from(comment);
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

    //댓글 조회
    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getComments(Long postId, Long currentUserId, Pageable pageable) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(PostErrorCode.POST_NOT_FOUND));

        validatePostAccess(post, currentUserId);

        return commentRepository.findAllByPostIdOrderByCreatedAtDesc(postId, pageable)
                .map(CommentResponseDto::from);
    }

    //내가 작성한 댓글 조회
    @Override
    public Page<CommentResponseDto> getMyComments(Long userId, Pageable pageable) {
        return commentRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(CommentResponseDto::from);
    }

    // =================================================================
    // 내부 검증 로직 (PostServiceImpl과 동일한 로직 사용)
    // =================================================================

    // 게시글 접근 권한 확인 (댓글 작성/조회 시 사용)
    private void validatePostAccess(Post post, Long currentUserId) {
        // 1. 로그인 체크 (댓글 기능은 무조건 로그인 필요)
        if (currentUserId == null) {
            throw new BusinessException(PostErrorCode.LOGIN_REQUIRED);
        }

        // 2. 작성자 본인은 프리패스
        if (post.getUser().getId().equals(currentUserId)) {
            return;
        }

        // 3. 무료 구독 확인
        Subscribe subscribe = validateSubscription(post.getUser().getId(), currentUserId);

        // 4. 유료 글이면 유료 구독 확인
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

        if (subscribe.getExpiredAt()!=null&&subscribe.getExpiredAt().isBefore(LocalDate.now())) {
            throw new BusinessException(PostErrorCode.SUBSCRIPTION_REQUIRED);
        }
        return subscribe;
    }
}