package com.backend.auth.repository;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisRefreshTokenStore implements RefreshTokenStore{
    private final StringRedisTemplate redisTemplate;

    private String key(Long userId) {
        return "refresh:user:" + userId;
    }

    @Override
    public void save(Long userId, String refreshToken, long ttlMs) {
        redisTemplate.opsForValue()
                .set(key(userId), refreshToken, Duration.ofMillis(ttlMs));
    }

    @Override
    public String get(Long userId) {
        return redisTemplate.opsForValue().get(key(userId));
    }

    @Override
    public void delete(Long userId) {
        redisTemplate.delete(key(userId));
    }
}
