package com.backend.auth.repository;


public interface RefreshTokenStore {

    void save(Long userId, String refreshToken, long ttlMs);

    String get(Long userId);

    void delete(Long userId);
}