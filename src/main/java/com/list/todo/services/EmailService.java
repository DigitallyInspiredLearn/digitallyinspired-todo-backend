package com.list.todo.services;

import com.list.todo.factory.MailMessageFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService{

    private final JavaMailSender mailSender;
    private final MailMessageFactory mailMessageFactory;

    @Value("${spring.mail.username}")
    private String username;

    @Async
    public void sendEmail(String emailTo, String subject, String text){
        MailMessage mailMessage = mailMessageFactory.getMailMessage();

        mailMessage.setFrom(username);
        mailMessage.setTo(emailTo);
        mailMessage.setSubject(subject);
        mailMessage.setText(text);

        mailSender.send((SimpleMailMessage) mailMessage);
    }
}
