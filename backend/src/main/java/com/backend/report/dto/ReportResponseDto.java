package com.backend.report.dto;

import com.backend.report.entity.Report;
import com.backend.report.entity.ReportType;

public record ReportResponseDto(
        Long id,
        Long userId,
        Long postId,
        Long commentId,
        ReportType type,
        String customReason
) {
    public static ReportResponseDto from(Report report){
        return new ReportResponseDto(
                report.getId(),
                report.getUser().getId(),
                report.getPost() != null ? report.getPost().getId() : null,
                report.getComment() != null ? report.getComment().getId() : null,
                report.getType(),
                report.getCustomReason()
        );
    }
}
