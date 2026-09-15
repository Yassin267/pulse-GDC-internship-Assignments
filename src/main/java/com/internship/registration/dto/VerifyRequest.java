package com.internship.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyRequest(
        @NotBlank(message = "code is required")
        @Pattern(regexp = "\\d{6}", message = "code must contain exactly 6 digits")
        String code
) {
}