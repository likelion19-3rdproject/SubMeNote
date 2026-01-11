package com.backend.global.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@RequiredArgsConstructor
public class MailSender {

    private final JavaMailSender mailSender;
    private static final String BODY = "<h3>요청하신 인증 번호입니다.</h3><br/>" +
                                        "<h1>%s</h1><br/>" +
                                        "<h3>감사합니다.</h3>";

    @Value("${spring.mail.username}")
    private String senderEmail;

    // 인증번호 생성 및 이메일 발송
    public String sendMessage(String sendEmail) {
        // 랜덤 인증번호 생성
        String authCode = createCode();

        // 이메일 발송
        try {
            MimeMessage message = createMail(sendEmail, authCode);
            mailSender.send(message);
            return authCode;
        } catch (MailException | MessagingException e) {
            return null;
        }
    }

    private String createCode() {
        Random random = new Random();
        StringBuffer key = new StringBuffer();

        for (int i = 0; i < 6; i++) { // 인증 코드 6자리
            key.append(random.nextInt(10)); // 숫자
        }

        return key.toString();
    }

    private MimeMessage createMail(String mail, String authCode) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        message.setFrom(senderEmail); // 발신자
        message.setRecipients(MimeMessage.RecipientType.TO, mail); // 수신자
        message.setSubject("회원가입 이메일 인증");

        message.setText(BODY.formatted(authCode), "UTF-8", "html");

        return message;
    }
}
