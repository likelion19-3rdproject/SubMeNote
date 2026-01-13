package com.backend.global;

import com.backend.global.properties.MailProperties;
import com.backend.global.properties.MailSmtpProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@EnableConfigurationProperties({MailProperties.class, MailSmtpProperties.class})
@RequiredArgsConstructor
public class MailConfig {

    private final MailProperties mailProperties;
    private final MailSmtpProperties mailSmtpProperties;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailProperties.host());
        mailSender.setPort(mailProperties.port());
        mailSender.setUsername(mailProperties.username());
        mailSender.setPassword(mailProperties.password());
        mailSender.setDefaultEncoding("UTF-8");
        mailSender.setJavaMailProperties(mailSmtpProperties.toJavaMailProperties());
        return mailSender;
    }
}
