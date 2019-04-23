package com.list.todo.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSenderMock;

    @InjectMocks
    private EmailService emailServiceMock;

    @Test
    public void sendEmail_SuccessfulSend() {
        //arrange
        String emailTo = "vv@g.com";
        String subject = "Home";
        String text = "someText";

        //act
        emailServiceMock.sendEmail(emailTo, subject, text);

        //assert
        verify(mailSenderMock, times(1)).send(any(SimpleMailMessage.class));
    }
}