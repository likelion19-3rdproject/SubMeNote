package com.backend.report.service;

import com.backend.report.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminReportService {

    Page<HiddenPostResponseDto> getHiddenPosts(Pageable pageable);

    Page<HiddenCommentResponseDto> getHiddenComments(Pageable pageable);

    void restore(ReportRestoreRequestDto requestDto);

    void delete(ReportDeleteRequestDto requestDto);
}
