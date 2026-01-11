package com.backend.comment.service;

import com.backend.comment.dto.CommentCreateRequestDto;
import com.backend.comment.dto.CommentResponseDto;
import com.backend.comment.dto.CommentUpdateRequestDto;
import com.backend.comment.entity.Comment;
import com.backend.comment.repository.CommentRepository;
import com.backend.post.entity.Post;
import com.backend.post.entity.PostVisibility;
import com.backend.post.repository.PostRepository;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    //댓글 생성(등록)
    @Override
    public CommentResponseDto create(Long postId, Long userId, CommentCreateRequestDto request) {
        //댓글을 달 게시글이 존재하는지 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("POST_NOT_FOUND"));

        //작성자(유저)가 존재하는지 확인
        // 로그인한 사용자 정보는 컨트롤러에서 @AuthenticationPrincipal로 전달됨
        // 여기서는 전달받은 userId가 실제 사용자로 존재하는지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));

        //게시글 접근권한 체크
        //1. 작성자는 무조건 댓글등록 가능
        if(post.getUser().getId().equals(user.getId())) {
        }
        else {
            //2. 구독 여부 검사 - 전체글과 구독자 전용글로 나뉘어있음
            if(post.getVisibility() == PostVisibility.SUBSCRIBERS_ONLY) {
                //todo 구독기능 구현되면 실제 구독여부 확인, 임시처리 중
                boolean isSubscriber = false;
                if(!isSubscriber) {
                    throw new IllegalArgumentException("POST_ACCESS_DENIED");
                }
            }
        }
        Comment comment = Comment.create(user, post, request.content());
        Comment saved = commentRepository.save(comment);

        return CommentResponseDto.from(saved);
    }

    //댓글 수정
    @Override
    public CommentResponseDto update(Long commentId, Long userId, CommentUpdateRequestDto request) {
        //수정할 댓글이 존재하는지 확인
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("COMMENT_NOT_FOUND"));

        //댓글작성자만 수정 가능
        if(!userId.equals(comment.getUser().getId())){
            throw new IllegalArgumentException("COMMENT_FORBIDDEN");
        }

        comment.update(request.content());

        return CommentResponseDto.from(comment);

    }

    //댓글 삭제
    @Override
    public void delete(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("COMMENT_NOT_FOUND"));

        //댓글작성자만 삭제 가능
        if (!userId.equals(comment.getUser().getId())) {
            throw new IllegalArgumentException("COMMENT_FORBIDDEN");
        }

        commentRepository.delete(comment); //하드삭제
    }

    //댓글 조회
    @Override
    public List<CommentResponseDto> getComments(Long postId) {
        // 게시글이 존재하는지 먼저 확인 (선택사항이지만 안전함)
        if (!postRepository.existsById(postId)) {
            throw new IllegalArgumentException("POST_NOT_FOUND");
        }

        // Repository에서 가져온 Entity 리스트를 DTO 리스트로 변환
        return commentRepository.findAllByPostIdOrderByCreatedAtDesc(postId)
                .stream()
                .map(CommentResponseDto::from)
                .collect(Collectors.toList());
    }

    //내가 작성한 댓글 조회
    @Override
    public List<CommentResponseDto> getMyComments(Long userId) {
        return commentRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(CommentResponseDto::from)
                .collect(Collectors.toList());
    }



    // 내부 메서드: 댓글 권한 검증
    private void validateCommentPermission(Post post, User user) {
        // 작성자는 항상 가능
        if (post.getUser().getId().equals(user.getId())) {
            return;
        }

        // 구독자 전용글 체크 로직 (필요시 구현)
        if (post.getVisibility() == PostVisibility.SUBSCRIBERS_ONLY) {
            // throw new IllegalArgumentException("POST_ACCESS_DENIED");
        }
    }
}