package com.backend.user.repository;

import com.backend.user.entity.CreatorApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<CreatorApplication, Long> {
    boolean existsByNickname(String nickname);

    Optional<CreatorApplication> findByUserId(Long userId);
}
