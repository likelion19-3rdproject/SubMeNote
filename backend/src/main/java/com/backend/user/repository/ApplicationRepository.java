package com.backend.user.repository;

import com.backend.user.entity.ApplicationStatus;
import com.backend.user.entity.CreatorApplication;
import com.backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<CreatorApplication, Long> {
    boolean existsByUserIdAndStatus(Long userId, ApplicationStatus status);

    Optional<CreatorApplication> findByUserId(Long userId);

    Page<CreatorApplication> findAllByUser(User user, Pageable pageable);
}
