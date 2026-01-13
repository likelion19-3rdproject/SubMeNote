package com.backend.email.repository;

import com.backend.email.entity.EmailAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailAuthRepository extends JpaRepository<EmailAuth, Long> {
    boolean existsByEmail(String email);

    Optional<EmailAuth> findByEmail(String email);
}
