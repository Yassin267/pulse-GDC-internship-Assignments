package com.internship.registration.dto;

import java.time.Instant;
import java.util.UUID;

public record VerificationResponse(
        UUID userId,
        String email,
        Instant createdAt
) {
}