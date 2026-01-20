package com.backend.report.repository;

import com.backend.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

    long countByPost_Id(Long post_id);

    long countByComment_Id(Long comment_id);

    void deleteByPost_Id(Long postId);

    void deleteByComment_Id(Long commentId);
}
