package com.backend.report.controller;

import com.backend.report.dto.HiddenCommentResponseDto;
import com.backend.report.dto.HiddenPostResponseDto;
import com.backend.report.dto.ReportDeleteRequestDto;
import com.backend.report.dto.ReportRestoreRequestDto;
import com.backend.report.service.ReportAdminService;
import com.backend.report.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/report/admin")
public class ReportAdminController {

    private final ReportAdminService reportAdminService;

    // 숨김 게시글 목록
    @GetMapping("/posts")
    public ResponseEntity<Page<HiddenPostResponseDto>> hiddenPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("createdAt")
                ));
        return ResponseEntity.ok(reportAdminService.getHiddenPosts(pageable));
    }

    // 숨김 댓글 목록
    @GetMapping("/comments")
    public ResponseEntity<Page<HiddenCommentResponseDto>> hiddenComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("createdAt")
                ));
        return ResponseEntity.ok(reportAdminService.getHiddenComments(pageable));
    }

    // 복구
    @PatchMapping("/restore")
    public ResponseEntity<Void> restore(@RequestBody @Valid ReportRestoreRequestDto request) {
        reportAdminService.restore(request);

        return ResponseEntity.noContent().build();
    }

    // 삭제
    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(@RequestBody @Valid ReportDeleteRequestDto request) {
        reportAdminService.delete(request);
        return ResponseEntity.noContent().build();
    }
}