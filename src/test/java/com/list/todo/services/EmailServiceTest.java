package com.list.todo.services;

import com.list.todo.factory.MailMessageFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class EmailServiceTest {

    @Value("${spring.mail.username}")
    private String username;

    @Mock
    private JavaMailSender mailSenderMock;

    @Mock
    private MailMessageFactory messageFactoryMock;

    @InjectMocks
    private EmailService emailServiceMock;

    @Test
    public void sendEmail_SuccessfulSend() {
        //arrange
        String emailTo = "vv@g.com";
        String subject = "Home";
        String text = "someText";

        MailMessage mailMessage = mock(SimpleMailMessage.class);

        when(messageFactoryMock.getMailMessage()).thenReturn(mailMessage);

        //act
        emailServiceMock.sendEmail(emailTo, subject, text);

        //assert
        verify(mailSenderMock, times(1)).send(any(SimpleMailMessage.class));
        verify(mailMessage, times(1)).setFrom(username);
        verify(mailMessage, times(1)).setTo(emailTo);
        verify(mailMessage, times(1)).setSubject(subject);
        verify(mailMessage, times(1)).setText(text);
    }
}