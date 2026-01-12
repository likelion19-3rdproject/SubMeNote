package com.backend.subscribe.repository;

import com.backend.subscribe.entity.Subscribe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscribeRepository extends JpaRepository<Subscribe, Long> {
    Optional<Subscribe> findByUser_IdAndCreator_Id(Long userId,Long creatorId);

    Page<Subscribe> findByUser_Id(Long userId, Pageable pageable);
}
