package com.backend.subscribe.repository;

import com.backend.subscribe.entity.Subscribe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscribeRepository extends JpaRepository<Subscribe, Long> {
    Optional<Subscribe> findByUser_IdAndCreator_Id(Long userId,Long creatorId);

    Page<Subscribe> findByUser_Id(Long userId, Pageable pageable);

    // 내가 구독 중이고(Active), 만료되지 않은 크리에이터의 ID만 리스트로 조회
    // JPQL을 사용하여 엔티티가 아닌 ID만 가져오기
    @Query("SELECT s.creator.id FROM Subscribe s " +
            "WHERE s.user.id = :userId " +
            "AND s.expiredAt > :now")
    List<Long> findCreatorIdsByUserIdAndExpiredAtAfter(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
