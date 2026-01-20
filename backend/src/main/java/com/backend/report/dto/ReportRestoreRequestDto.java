package com.backend.report.dto;

import com.backend.report.entity.ReportType;
import jakarta.validation.constraints.NotNull;

public record ReportRestoreRequestDto(

        @NotNull
        ReportType type,

        @NotNull
        Long targetId
) {
}