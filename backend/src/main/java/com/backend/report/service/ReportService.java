package com.backend.report.service;

import com.backend.report.dto.ReportResponseDto;
import com.backend.report.entity.ReportType;

public interface ReportService {
    ReportResponseDto createReport(
            Long userId,
            Long targetId,
            ReportType type,
            String customReason
    );
}
