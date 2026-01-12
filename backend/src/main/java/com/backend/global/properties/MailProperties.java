package com.backend.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.mail")
public record MailProperties(
        String host,
        int port,
        String username,
        String password
) {
}
