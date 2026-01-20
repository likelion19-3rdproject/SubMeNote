package com.backend.subscribe.repository;

import com.backend.subscribe.entity.Subscribe;
import com.backend.subscribe.entity.SubscribeStatus;
import com.backend.subscribe.entity.SubscribeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscribeRepository extends JpaRepository<Subscribe, Long> {

    Optional<Subscribe> findByUser_IdAndCreator_Id(Long userId, Long creatorId);

    Page<Subscribe> findByUser_Id(Long userId, Pageable pageable);

    // 회원 탈퇴시 사용
    long countByCreator_IdAndStatus(Long creatorId, SubscribeStatus status);

    long countByUser_IdAndStatusAndType(Long userId, SubscribeStatus status, SubscribeType type);

    @Query("""
            SELECT s.creator.id FROM Subscribe s
            WHERE s.user.id = :userId 
            """)
    List<Long> findCreatorIdsByUserId(@Param("userId") Long userId);

    //만료일이 targetDate 인 구독 (알림)
    @Query("""
            select s from Subscribe s 
            where s.status = :status
            and s.expiredAt = :targetDate
            """)
    List<Subscribe> findExpiringAt(
            @Param("status") SubscribeStatus status,
            @Param("targetDate") LocalDate targetDate
    );

    //만료일이 지난 구독
    @Query("""
            select s from Subscribe s 
            where s.status = :status 
            and s.expiredAt < :today
            """)
    List<Subscribe> findExpiredBefore(
            @Param("status") SubscribeStatus status,
            @Param("today") LocalDate today
    );
}
