package com.backend.global;

import com.backend.global.jwt.JwtAuthenticationFilter;
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
                        .requestMatchers("/api/home").permitAll() // 홈화면 전체 공개
                        .requestMatchers("/api/users/me/**").authenticated() // 회원 탈퇴, 내 게시글 조회, 내 댓글 조회는 인증필요
                        .requestMatchers("/api/auth/**", "/api/email/**").permitAll() // 회원가입, 이메일 인증 등 인증 불필요
                        .requestMatchers(HttpMethod.GET, "api/posts/**").permitAll() // 현재 비로그인/로그인 postservice로직에서 판단
                        .requestMatchers(HttpMethod.POST, "api/posts/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "api/posts/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "api/posts/**").authenticated()

                        .requestMatchers("/api/subscribes/**").authenticated()// 구독 관련 기능은 로그인 필요
                        .requestMatchers("/confirm", "/fail").permitAll() // 결제 확인은 외부에서 호출되므로 permitAll
                        .anyRequest().authenticated() // 그 외 요청은 인증 필요
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
