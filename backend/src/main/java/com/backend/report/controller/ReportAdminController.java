package com.backend.report.controller;

import com.backend.report.dto.HiddenCommentResponseDto;
import com.backend.report.dto.HiddenPostResponseDto;
import com.backend.report.dto.ReportDeleteRequestDto;
import com.backend.report.dto.ReportRestoreRequestDto;
import com.backend.report.service.impl.AdminReportServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/report")
public class ReportAdminController {

    private final AdminReportServiceImpl adminReportServiceImpl;

    /**
     * 신고된 게시글 목록
     */
    @GetMapping("/posts")
    public ResponseEntity<Page<HiddenPostResponseDto>> hiddenPosts(
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(adminReportServiceImpl.getHiddenPosts(pageable));
    }

    /**
     * 신고된 댓글 목록
     */
    @GetMapping("/comments")
    public ResponseEntity<Page<HiddenCommentResponseDto>> hiddenComments(
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(adminReportServiceImpl.getHiddenComments(pageable));
    }

    /**
     * 복구
     */
    @PatchMapping("/restore")
    public ResponseEntity<Void> restore(
            @RequestBody @Valid ReportRestoreRequestDto request
    ) {

        adminReportServiceImpl.restore(request);

        return ResponseEntity.noContent().build();
    }

    /**
     * 삭제
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(
            @RequestBody @Valid ReportDeleteRequestDto request
    ) {

        adminReportServiceImpl.delete(request);

        return ResponseEntity.noContent().build();
    }
}