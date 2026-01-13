package com.backend.email.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_auth")
@Getter
@NoArgsConstructor
public class EmailAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "auth_code", nullable = false)
    private String authCode;

    // 인증 완료 여부
    @Column(name = "verified", nullable = false)
    private boolean verified;

    // 만료 시간
    @Column(name = "expire_at", nullable = false)
    private LocalDateTime expireAt;

    public EmailAuth(String email, String authCode) {
        this.email = email;
        this.authCode = authCode;
        this.verified = false;
        this.expireAt = LocalDateTime.now().plusMinutes(5);
    }

    // 인증 완료
    public void markAsVerified() {
        this.verified = true;
    }

    // 인증 코드 만료
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expireAt);
    }
}
