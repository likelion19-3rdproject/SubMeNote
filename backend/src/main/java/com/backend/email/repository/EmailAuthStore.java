package com.backend.email.repository;

import com.backend.email.entity.EmailAuth;

public interface EmailAuthStore {

    // 인증코드 관련
    boolean existsCode(String email);
    void saveCode(String email, String authCode, long ttlMs);
    String getCode(String email);
    void deleteCode(String email);

    // 인증완료(verified) 관련
    void saveVerified(String email, long ttlMs);
    boolean isVerified(String email);
    void deleteVerified(String email);

}
