package com.backend.auth.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class JwtConfig {

    @Bean
    public JwtProvider jwtProvider(
            @Value("${JWT_SECRET}") String secret,
            @Value("${JWT_ACCESS_TOKEN_MS}") long accessMs,
            @Value("${JWT_REFRESH_TOKEN_MS}") long refreshMs
    ) {
        return new JwtProvider(secret, accessMs, refreshMs);
    }
}