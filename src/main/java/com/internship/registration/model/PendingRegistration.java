package com.internship.registration.model;

import java.time.Instant;
import java.util.UUID;

public record PendingRegistration(
        UUID registrationId,
        String email,
        String passwordHash,
        Instant createdAt,
        Instant expiresAt,
        Instant resendAvailableAt
) {
}