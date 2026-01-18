package com.backend.auth.repository;


import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisRefreshTokenStore implements RefreshTokenStore{
    private final StringRedisTemplate redisTemplate;

    public RedisRefreshTokenStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

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
