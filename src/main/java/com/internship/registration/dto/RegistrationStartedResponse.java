package com.internship.registration.dto;

import java.time.Instant;
import java.util.UUID;

public record RegistrationStartedResponse(
        UUID registrationId,
        Instant expiresAt,
        Instant resendAvailableAt
) {
}