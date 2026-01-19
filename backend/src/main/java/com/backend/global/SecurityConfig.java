package com.backend.global;

import com.backend.global.jwt.JwtAuthenticationFilter;
import com.backend.global.util.CustomAccessDeniedHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;



    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // 1. 기본 보안 설정 비활성화 (JWT 사용)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 2. CORS 설정 적용 (CorsConfig 파일의 설정 사용)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // 3. 세션 관리 상태 없음(Stateless) 설정
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // 4. 경로별 인가(Authorization) 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/report/admin/**").hasRole("ADMIN")
                        // [공개] 인증/이메일 관련 API는 누구나 접근 가능
                        .requestMatchers("/api/auth/**", "/api/email/**").permitAll()

                        // [공개] 메인 홈 (크리에이터 목록 조회 및 검색)
                        .requestMatchers(HttpMethod.GET, "/api/home", "/api/home/search").permitAll()

                        // [공개] 프로필 이미지 조회 (누구나 볼 수 있어야 함)
                        .requestMatchers(HttpMethod.GET, "/api/profile-images/**").permitAll()

                        // [조회 - 중요] 게시글, 댓글, 크리에이터별 글 목록 조회
                        .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/creators/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/comments/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/*/comments").permitAll()

                        // [결제 - 리다이렉트] 토스 페이먼츠에서 성공/실패 시 돌아오는 URL
                        .requestMatchers("/confirm", "/fail").permitAll()



                        // [결제 - 승인] 실제 돈이 나가는 결제 승인 요청은 로그인한 유저만 가능
                        .requestMatchers(HttpMethod.POST, "/api/payments/confirm").authenticated()

                        // [주문] 주문 생성 및 내역 조회는 개인 정보이므로 인증 필요
                        .requestMatchers("/api/orders/**").authenticated()

                        // [구독] 구독하기, 구독 취소, 내 구독 목록 등
                        .requestMatchers("/api/subscribes/**").authenticated()

                        // [마이페이지/정산] 내 정보, 정산 내역 등
                        .requestMatchers("/api/users/me/**").authenticated()
                        .requestMatchers("/api/settlements/**").authenticated()

                        // [작성/수정/삭제] 게시글 및 댓글의 CUD 작업은 무조건 인증 필요
                        .requestMatchers(HttpMethod.POST, "/api/posts/**", "/api/comments/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/posts/**", "/api/comments/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/**", "/api/comments/**").authenticated()

                        // 그 외 정의되지 않은 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // 5. JWT 필터 추가 (UsernamePasswordAuthenticationFilter 앞에서 실행)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}