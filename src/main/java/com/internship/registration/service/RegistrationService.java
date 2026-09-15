package com.internship.registration.service;

import com.internship.registration.config.RegistrationProperties;
import com.internship.registration.dto.RegistrationStartedResponse;
import com.internship.registration.dto.RegistrationResentResponse;
import com.internship.registration.dto.StartRegistrationRequest;
import com.internship.registration.dto.VerificationResponse;
import com.internship.registration.dto.VerifyRequest;
import com.internship.registration.exception.RegistrationException;
import com.internship.registration.model.OtpState;
import com.internship.registration.model.PendingRegistration;
import com.internship.registration.repository.UserRepository;
import com.internship.registration.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegistrationProperties properties;
    private final PendingRegistrationStore registrationStore;
    private final OtpGenerator otpGenerator;
    private final EmailService emailService;
    private final Clock clock;

    public RegistrationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RegistrationProperties properties,
            PendingRegistrationStore registrationStore,
            OtpGenerator otpGenerator,
            EmailService emailService,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
        this.registrationStore = registrationStore;
        this.otpGenerator = otpGenerator;
        this.emailService = emailService;
        this.clock = clock;
    }

    public RegistrationStartedResponse start(StartRegistrationRequest request) {
        String email = normalizeEmail(request.email());
        Instant now = Instant.now(clock);
        Instant expiresAt = now.plus(properties.getPendingLifetime());
        Instant resendAvailableAt = now.plus(properties.getOtp().getResendCooldown());

        if (userRepository.existsByEmail(email)) {
            emailService.sendExistingAddressNotice(email);
            return new RegistrationStartedResponse(UUID.randomUUID(), expiresAt, resendAvailableAt);
        }

        enforceSendLimit(email);

        UUID registrationId = UUID.randomUUID();
        String code = otpGenerator.generate();
        PendingRegistration pending = new PendingRegistration(
                registrationId,
                email,
                passwordEncoder.encode(request.password()),
                now,
                expiresAt,
                resendAvailableAt
        );

        registrationStore.savePending(pending, properties.getPendingLifetime());
        registrationStore.saveOtp(
                registrationId,
                new OtpState(passwordEncoder.encode(code), 0),
                properties.getOtp().getLifetime()
        );

        try {
            emailService.sendVerificationCode(email, code);
        } catch (RuntimeException exception) {
            registrationStore.deleteOtp(registrationId);
            registrationStore.deletePending(registrationId);
            throw exception;
        }

        return new RegistrationStartedResponse(registrationId, expiresAt, resendAvailableAt);
    }

    public RegistrationResentResponse resend(UUID registrationId) {
        if (registrationStore.isCompleted(registrationId)) {
            throw new RegistrationException(
                    2002,
                    HttpStatus.CONFLICT,
                    "This registration has already been completed."
            );
        }

        PendingRegistration pending = registrationStore.findPending(registrationId)
                .orElseThrow(() -> new RegistrationException(
                        2001,
                        HttpStatus.NOT_FOUND,
                        "No pending registration with that id."
                ));
        Instant now = Instant.now(clock);
        if (now.isBefore(pending.resendAvailableAt())) {
            throw new RegistrationException(
                    4001,
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Please wait before requesting another code."
            );
        }

        enforceSendLimit(pending.email());
        String code = otpGenerator.generate();
        Instant resendAvailableAt = now.plus(properties.getOtp().getResendCooldown());
        Duration pendingLifetime = registrationStore.pendingTimeToLive(registrationId);
        PendingRegistration updated = new PendingRegistration(
                pending.registrationId(),
                pending.email(),
                pending.passwordHash(),
                pending.createdAt(),
                pending.expiresAt(),
                resendAvailableAt
        );

        registrationStore.savePending(updated, pendingLifetime);
        registrationStore.saveOtp(
                registrationId,
                new OtpState(passwordEncoder.encode(code), 0),
                properties.getOtp().getLifetime()
        );

        try {
            emailService.sendVerificationCode(pending.email(), code);
        } catch (RuntimeException exception) {
            throw exception;
        }

        return new RegistrationResentResponse(pending.expiresAt(), resendAvailableAt);
    }

        @Transactional
        public VerificationResponse verify(UUID registrationId, VerifyRequest request) {
        if (registrationStore.isCompleted(registrationId)) {
            throw new RegistrationException(
                2002,
                HttpStatus.CONFLICT,
                "This registration has already been completed."
            );
        }

        PendingRegistration pending = registrationStore.findPending(registrationId)
            .orElseThrow(() -> new RegistrationException(
                2001,
                HttpStatus.NOT_FOUND,
                "No pending registration with that id."
            ));

        OtpState otp = registrationStore.findOtp(registrationId)
            .orElseThrow(() -> new RegistrationException(
                3002,
                HttpStatus.GONE,
                "The code has expired."
            ));

        if (!passwordEncoder.matches(request.code(), otp.codeHash())) {
            int attempts = otp.attempts() + 1;
            if (attempts >= properties.getOtp().getMaxAttempts()) {
            registrationStore.deleteOtp(registrationId);
            throw new RegistrationException(
                3003,
                HttpStatus.TOO_MANY_REQUESTS,
                "Too many incorrect code attempts."
            );
            }

            Duration remaining = registrationStore.otpTimeToLive(registrationId);
            registrationStore.saveOtp(registrationId, new OtpState(otp.codeHash(), attempts), remaining);
            throw new RegistrationException(
                3001,
                HttpStatus.BAD_REQUEST,
                "The code is not correct."
            );
        }

        Instant createdAt = Instant.now(clock);
        User user = userRepository.save(new User(pending.email(), pending.passwordHash(), createdAt));
        registrationStore.deleteOtp(registrationId);
        registrationStore.deletePending(registrationId);
        registrationStore.markCompleted(registrationId, properties.getPendingLifetime());

        return new VerificationResponse(user.getId(), user.getEmail(), user.getCreatedAt());
        }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void enforceSendLimit(String email) {
        long sendCount = registrationStore.incrementSendCount(
                email,
                properties.getOtp().getDailySendWindow()
        );
        if (sendCount > properties.getOtp().getDailySendLimit()) {
            throw new RegistrationException(
                    4002,
                    HttpStatus.TOO_MANY_REQUESTS,
                    "The daily code send limit has been reached."
            );
        }
    }
}