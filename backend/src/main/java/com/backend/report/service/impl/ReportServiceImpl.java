package com.backend.report.service.impl;

import com.backend.comment.entity.Comment;
import com.backend.comment.repository.CommentRepository;
import com.backend.global.exception.domain.CommentErrorCode;
import com.backend.global.exception.domain.PostErrorCode;
import com.backend.global.exception.domain.ReportErrorCode;
import com.backend.global.exception.common.BusinessException;
import com.backend.notification.dto.NotificationContext;
import com.backend.notification.entity.NotificationType;
import com.backend.notification.entity.NotificationTargetType;
import com.backend.notification.service.NotificationCommand;
import com.backend.post.entity.Post;
import com.backend.post.repository.PostRepository;
import com.backend.report.dto.ReportResponseDto;
import com.backend.report.entity.Report;
import com.backend.report.entity.ReportType;
import com.backend.report.repository.ReportRepository;
import com.backend.report.service.ReportService;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final NotificationCommand notificationCommand;

    private final long HIDDEN = 1L;

    /**
     * 신고
     */
    @Override
    @Transactional
    public ReportResponseDto createReport(Long userId, Long targetId, ReportType type, String customReason) {

        User user = userRepository.findByIdOrThrow(userId);

        Report report;

        report = getReport(userId, targetId, type, customReason, user);

        return ReportResponseDto.from(report);
    }


    private Report getReport(Long userId, Long targetId, ReportType type, String customReason, User user) {

        Report report;

        try {
            report = switch (type) {

                case POST -> {
                    Post post = postRepository.findById(targetId)
                            .orElseThrow(() -> new BusinessException(PostErrorCode.POST_NOT_FOUND));

                    if (post.getUser().getId().equals(userId)) {
                        throw new BusinessException(ReportErrorCode.CANNOT_REPORT_SELF);
                    }

                    Report reportPost = Report.of(user, post, null, type, customReason);
                    Report saved = reportRepository.save(reportPost);
                    reportRepository.flush();

                    long i = reportRepository.countByPost_Id(targetId);

                    if (i >= HIDDEN) {
                        post.hiddenPost();
                    }

                    notificationCommand.createNotification(
                            post.getUser().getId(),
                            NotificationType.POST_REPORTED,
                            NotificationTargetType.POST,
                            targetId,
                            NotificationContext.forReport(post.getTitle())
                    );

                    yield saved;
                }

                case COMMENT -> {
                    Comment comment = commentRepository.findById(targetId)
                            .orElseThrow(() -> new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND));

                    if (comment.getUser().getId().equals(userId)) {
                        throw new BusinessException(ReportErrorCode.CANNOT_REPORT_SELF);
                    }

                    Report reportComment = Report.of(user, null, comment, type, customReason);
                    Report saved = reportRepository.save(reportComment);
                    reportRepository.flush();

                    long i = reportRepository.countByComment_Id(targetId);

                    if (i >= HIDDEN) {
                        comment.hiddenComment();
                    }

                    notificationCommand.createNotification(
                            comment.getUser().getId(),
                            NotificationType.COMMENT_REPORTED,
                            NotificationTargetType.POST,
                            comment.getPost().getId(),
                            NotificationContext.forReport(comment.getContent())
                    );

                    yield saved;
                }
            };
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ReportErrorCode.ALREADY_REPORTED);
        }

        return report;
    }
}
