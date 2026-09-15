package com.internship.registration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "registration")
public class RegistrationProperties {

    private Duration pendingLifetime;
    private int passwordMinLength;
    private String mailFrom;
    private final Otp otp = new Otp();

    public Duration getPendingLifetime() {
        return pendingLifetime;
    }

    public void setPendingLifetime(Duration pendingLifetime) {
        this.pendingLifetime = pendingLifetime;
    }

    public int getPasswordMinLength() {
        return passwordMinLength;
    }

    public void setPasswordMinLength(int passwordMinLength) {
        this.passwordMinLength = passwordMinLength;
    }

    public String getMailFrom() {
        return mailFrom;
    }

    public void setMailFrom(String mailFrom) {
        this.mailFrom = mailFrom;
    }

    public Otp getOtp() {
        return otp;
    }

    public static class Otp {

        private int length;
        private Duration lifetime;
        private int maxAttempts;
        private Duration resendCooldown;
        private int dailySendLimit;
        private Duration dailySendWindow;

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public Duration getLifetime() {
            return lifetime;
        }

        public void setLifetime(Duration lifetime) {
            this.lifetime = lifetime;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public Duration getResendCooldown() {
            return resendCooldown;
        }

        public void setResendCooldown(Duration resendCooldown) {
            this.resendCooldown = resendCooldown;
        }

        public int getDailySendLimit() {
            return dailySendLimit;
        }

        public void setDailySendLimit(int dailySendLimit) {
            this.dailySendLimit = dailySendLimit;
        }

        public Duration getDailySendWindow() {
            return dailySendWindow;
        }

        public void setDailySendWindow(Duration dailySendWindow) {
            this.dailySendWindow = dailySendWindow;
        }
    }
}