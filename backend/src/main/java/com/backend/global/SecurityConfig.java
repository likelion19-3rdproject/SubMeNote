package com.backend.global;

import com.backend.auth.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/me/**").authenticated() //회원 탈퇴, 내 글 조회, 내 댓글 조회는 인증필요
                        .requestMatchers("/api/auth/**", "/api/email/**", "/api/users/**").permitAll() //회원가입, 이메일 인증, 크리에이터 목록 등 인증 불필요
                        .requestMatchers(HttpMethod.GET, "/posts/**").permitAll() //현재 비로그인/로그인 postservice로직에서 판단
                        .requestMatchers(HttpMethod.POST, "/posts/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/posts/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/posts/**").authenticated()

                        .requestMatchers("/api/subscribes/**").authenticated()//구독 관련 기능은 로그인 필요
                        .anyRequest().authenticated() //그 외 요청은 인증 필요

                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
