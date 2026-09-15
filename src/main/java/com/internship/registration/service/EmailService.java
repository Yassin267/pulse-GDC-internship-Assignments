package com.internship.registration.service;

import com.internship.registration.config.RegistrationProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final RegistrationProperties properties;

    public EmailService(JavaMailSender mailSender, RegistrationProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    public void sendVerificationCode(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getMailFrom());
        message.setTo(email);
        message.setSubject("Your registration verification code");
        message.setText("Your verification code is: " + code);
        mailSender.send(message);
    }

    public void sendExistingAddressNotice(String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getMailFrom());
        message.setTo(email);
        message.setSubject("Someone tried to register with your email address");
        message.setText("Someone tried to create an account using this email address. If this was not you, you can ignore this message.");
        mailSender.send(message);
    }
}