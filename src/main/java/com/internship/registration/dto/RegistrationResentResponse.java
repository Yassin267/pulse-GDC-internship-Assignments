package com.internship.registration.dto;

import java.time.Instant;

public record RegistrationResentResponse(
        Instant expiresAt,
        Instant resendAvailableAt
) {
}