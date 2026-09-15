package com.internship.registration.controller;

import com.internship.registration.dto.ApiResponse;
import com.internship.registration.dto.RegistrationStartedResponse;
import com.internship.registration.dto.RegistrationResentResponse;
import com.internship.registration.dto.StartRegistrationRequest;
import com.internship.registration.dto.VerificationResponse;
import com.internship.registration.dto.VerifyRequest;
import com.internship.registration.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/registrations")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RegistrationStartedResponse>> start(
            @Valid @RequestBody StartRegistrationRequest request
    ) {
        RegistrationStartedResponse result = registrationService.start(request);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(result));
    }

    @PostMapping("/{registrationId}/verify")
    public ResponseEntity<ApiResponse<VerificationResponse>> verify(
            @PathVariable UUID registrationId,
            @Valid @RequestBody VerifyRequest request
    ) {
        VerificationResponse result = registrationService.verify(registrationId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(result));
    }

    @PostMapping("/{registrationId}/resend")
    public ResponseEntity<ApiResponse<RegistrationResentResponse>> resend(
            @PathVariable UUID registrationId
    ) {
        RegistrationResentResponse result = registrationService.resend(registrationId);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(result));
    }
}