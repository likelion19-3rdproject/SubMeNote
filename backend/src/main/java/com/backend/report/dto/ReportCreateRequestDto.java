package com.backend.report.dto;


import com.backend.report.entity.ReportType;
import jakarta.validation.constraints.NotNull;

public record ReportCreateRequestDto(
        @NotNull Long targetId,
        @NotNull ReportType type,
        String customReason
) {}
