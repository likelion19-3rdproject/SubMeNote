package com.backend.subscribe.repository;

import com.backend.subscribe.entity.Subscribe;
import com.backend.subscribe.entity.SubscribeStatus;
import com.backend.subscribe.entity.SubscribeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscribeRepository extends JpaRepository<Subscribe, Long> {
    Optional<Subscribe> findByUser_IdAndCreator_Id(Long userId,Long creatorId);

    Page<Subscribe> findByUser_Id(Long userId, Pageable pageable);

    // 회원 탈퇴시 사용
    long countByCreator_IdAndStatus(Long creatorId, SubscribeStatus status);

    long countByUser_IdAndStatusAndType(Long userId, SubscribeStatus status, SubscribeType type);
}
