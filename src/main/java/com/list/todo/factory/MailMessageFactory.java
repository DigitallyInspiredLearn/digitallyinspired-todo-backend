package com.list.todo.factory;

import org.springframework.mail.MailMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

@Component
public class MailMessageFactory {
    public MailMessage createNewMailMessage() {
        return new SimpleMailMessage();
    }
}
