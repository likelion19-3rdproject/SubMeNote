package com.backend.report.service.impl;

import com.backend.comment.entity.Comment;
import com.backend.comment.entity.CommentReportStatus;
import com.backend.comment.repository.CommentRepository;
import com.backend.global.exception.domain.CommentErrorCode;
import com.backend.global.exception.domain.PostErrorCode;
import com.backend.global.exception.domain.ReportErrorCode;
import com.backend.global.exception.common.BusinessException;
import com.backend.post.entity.Post;
import com.backend.post.entity.PostReportStatus;
import com.backend.post.repository.PostRepository;
import com.backend.report.dto.HiddenCommentResponseDto;
import com.backend.report.dto.HiddenPostResponseDto;
import com.backend.report.dto.ReportDeleteRequestDto;
import com.backend.report.dto.ReportRestoreRequestDto;
import com.backend.report.repository.ReportRepository;
import com.backend.report.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminReportServiceImpl implements AdminReportService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;

    /**
     * 게시글 신고 조회
     */
    @Override
    @Transactional(readOnly = true)
    public Page<HiddenPostResponseDto> getHiddenPosts(Pageable pageable) {

        return postRepository
                .findByStatus(PostReportStatus.REPORT, pageable)
                .map(HiddenPostResponseDto::from);
    }

    /**
     * 댓글 신고 조회
     */
    @Override
    @Transactional(readOnly = true)
    public Page<HiddenCommentResponseDto> getHiddenComments(Pageable pageable) {

        return commentRepository
                .findByStatus(CommentReportStatus.REPORT, pageable)
                .map(HiddenCommentResponseDto::from);
    }

    /**
     * 복구
     */
    @Override
    @Transactional
    public void restore(ReportRestoreRequestDto requestDto) {

        switch (requestDto.type()) {
            case POST -> restorePost(requestDto.targetId());
            case COMMENT -> restoreComment(requestDto.targetId());
            default -> throw new BusinessException(ReportErrorCode.REPORT_TARGET_NOT_FOUND);
        }
    }

    /**
     * 삭제
     */
    @Override
    @Transactional
    public void delete(ReportDeleteRequestDto requestDto) {

        switch (requestDto.type()) {
            case POST -> deletePost(requestDto.targetId());
            case COMMENT -> deleteComment(requestDto.targetId());
            default -> throw new BusinessException(ReportErrorCode.REPORT_TARGET_NOT_FOUND);
        }
    }

    private void restorePost(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(PostErrorCode.POST_NOT_FOUND));

        if (post.getStatus() != PostReportStatus.REPORT) {
            throw new BusinessException(ReportErrorCode.NOT_REPORTED_OBJECT);
        }

        post.restorePost();

        reportRepository.deleteByPost_Id(postId);
    }

    private void restoreComment(Long commentId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND));

        if (comment.getStatus() != CommentReportStatus.REPORT) {
            throw new BusinessException(ReportErrorCode.NOT_REPORTED_OBJECT);
        }

        comment.restoreComment();

        reportRepository.deleteByComment_Id(commentId);
    }

    private void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(PostErrorCode.POST_NOT_FOUND));

        if(post.getStatus()!=PostReportStatus.REPORT){
            throw new BusinessException(ReportErrorCode.NOT_REPORTED_OBJECT);
        }

        postRepository.delete(post);
    }

    private void deleteComment(Long commentId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND));

        if (comment.getStatus() != CommentReportStatus.REPORT) {
            throw new BusinessException(ReportErrorCode.NOT_REPORTED_OBJECT);
        }

        commentRepository.delete(comment);
    }
}