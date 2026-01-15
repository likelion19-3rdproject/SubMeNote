package com.backend.user.repository;

import com.backend.user.entity.ApplicationStatus;
import com.backend.user.entity.CreatorApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<CreatorApplication, Long> {
    boolean existsByUserIdAndStatus(Long userId, ApplicationStatus status);

    Optional<CreatorApplication> findByUserId(Long userId);

    Page<CreatorApplication> findByStatusOrderByAppliedAt(ApplicationStatus status, Pageable pageable);
}
