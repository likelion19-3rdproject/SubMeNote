package com.backend.user.repository;

import com.backend.user.entity.EmailAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailAuthRepository extends JpaRepository<EmailAuth, Long> {
    boolean existsByEmail(String email);

    Optional<EmailAuth> findByEmail(String email);
}
