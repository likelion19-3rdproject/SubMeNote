package com.backend.comment.repository;

import com.backend.comment.entity.Comment;
import com.backend.comment.entity.CommentReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 내가 쓴 댓글 조회 (최신순)
    Page<Comment> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Comment> findByStatus(CommentReportStatus status, Pageable pageable);

    @Query("""
            select c from Comment c
            join fetch c.user
            where c.post.id = :postId and c.parent is null
            order by c.createdAt desc
            """)
    Page<Comment> findRootsWithUser(@Param("postId") Long postId, Pageable pageable);

    @Query("""
            select c from Comment c
            join fetch c.user
            where c.parent.id in :parentIds
            order by c.createdAt asc
            """)
    List<Comment> findChildrenWithUser(@Param("parentIds") List<Long> parentIds);

}