package com.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 1. 프론트엔드 주소 (Next.js) 허용
        // allowCredentials=true 이므로 "*"는 사용할 수 없어, 패턴 기반으로 허용합니다.
        // - 로컬 개발: localhost:3000
        // - 배포(EC2): 공인 IP/도메인:3000
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "http://3.106.134.167:3000",
                "https://3.106.134.167:3000",
                "http://*.amazonaws.com:3000",
                "https://*.amazonaws.com:3000"
        ));

        // 2. 허용할 HTTP 메서드
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 3. 허용할 헤더
        config.setAllowedHeaders(List.of("*"));

        // 4. 쿠키 주고받기 허용
        config.setAllowCredentials(true);

        // 5. 브라우저에 노출할 헤더 (Set-Cookie 확인용)
        config.setExposedHeaders(List.of("Set-Cookie", "Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}