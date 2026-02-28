package org.strelnikova.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEmail(NotificationMessageBuilder.EmailData emailData) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailData.to());
        message.setSubject(emailData.subject());
        message.setText(emailData.body());

        mailSender.send(message);
        log.info("Email sent to {}", emailData.to());
    }

}