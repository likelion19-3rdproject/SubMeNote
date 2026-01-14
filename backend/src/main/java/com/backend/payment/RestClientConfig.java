package com.backend.payment;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient() {
        // 타임아웃 설정을 위한 RequestFactory 생성
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 연결 타임아웃 5초 (우리 서버가 토스 서버 문 두드리는 시간)
        factory.setReadTimeout(30000);   // 데이터 읽기 타임아웃 30초 (연결 이후 토스가 카드사에 결제요청 후 결과를 기다리는 시간)

        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }
}