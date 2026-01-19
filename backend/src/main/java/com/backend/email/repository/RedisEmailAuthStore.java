package com.backend.email.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisEmailAuthStore implements EmailAuthStore {

    private final StringRedisTemplate redisTemplate;

    private String codeKey(String email) {
        return "email:code:" + email;
    }

    private String verifiedKey(String email) {
        return "email:verified:" + email;
    }

    @Override
    public boolean existsCode(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(codeKey(email)));
    }

    @Override
    public void saveCode(String email, String authCode, long ttlMs) {
        redisTemplate.opsForValue()
                .set(codeKey(email), authCode, Duration.ofMillis(ttlMs));
    }

    @Override
    public String getCode(String email) {
        return redisTemplate.opsForValue().get(codeKey(email));
    }

    @Override
    public void deleteCode(String email) {
        redisTemplate.delete(codeKey(email));
    }

    @Override
    public void saveVerified(String email, long ttlMs) {
        redisTemplate.opsForValue()
                .set(verifiedKey(email), "1", Duration.ofMillis(ttlMs));
    }

    @Override
    public boolean isVerified(String email) {
        String v = redisTemplate.opsForValue().get(verifiedKey(email));
        return "1".equals(v);
    }

    @Override
    public void deleteVerified(String email) {
        redisTemplate.delete(verifiedKey(email));
    }
}
