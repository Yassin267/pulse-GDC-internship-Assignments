package com.internship.registration.service;

import com.internship.registration.config.RegistrationProperties;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class OtpGenerator {

    private final SecureRandom random = new SecureRandom();
    private final RegistrationProperties properties;

    public OtpGenerator(RegistrationProperties properties) {
        this.properties = properties;
    }

    public String generate() {
        int length = properties.getOtp().getLength();
        StringBuilder code = new StringBuilder(length);

        for (int index = 0; index < length; index++) {
            code.append(random.nextInt(10));
        }

        return code.toString();
    }
}