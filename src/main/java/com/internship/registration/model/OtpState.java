package com.internship.registration.model;

public record OtpState(String codeHash, int attempts) {
}