package com.backend.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

@ConfigurationProperties(prefix = "spring.mail.properties.mail.smtp")
public record MailSmtpProperties(
        boolean auth,
        boolean starttlsEnable,
        boolean starttlsRequired,
        int connectiontimeout,
        int timeout,
        int writetimeout
) {

    public Properties toJavaMailProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", auth);
        props.put("mail.smtp.starttls.enable", starttlsEnable);
        props.put("mail.smtp.starttls.required", starttlsRequired);
        props.put("mail.smtp.connectiontimeout", connectiontimeout);
        props.put("mail.smtp.timeout", timeout);
        props.put("mail.smtp.writetimeout", writetimeout);
        return props;
    }
}
