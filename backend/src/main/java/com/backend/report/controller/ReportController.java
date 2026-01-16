package com.backend.report.controller;

import com.backend.global.util.CustomUserDetails;
import com.backend.report.dto.*;
import com.backend.report.service.ReportAdminService;
import com.backend.report.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ReportResponseDto> createReport(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ReportCreateRequestDto request
    ) {
        ReportResponseDto response = reportService.createReport(
                userDetails.getUserId(),
                request.targetId(),
                request.type(),
                request.customReason()
        );
        return ResponseEntity.ok(response);
    }
}