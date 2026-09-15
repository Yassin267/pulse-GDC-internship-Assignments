package com.internship.registration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.registration.model.OtpState;
import com.internship.registration.model.PendingRegistration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class PendingRegistrationStore {

    private static final String PENDING_PREFIX = "registration:pending:";
    private static final String OTP_PREFIX = "registration:otp:";
    private static final String COMPLETED_PREFIX = "registration:completed:";
    private static final String SEND_COUNT_PREFIX = "registration:send-count:";

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public PendingRegistrationStore(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    public void savePending(PendingRegistration registration, Duration lifetime) {
        put(pendingKey(registration.registrationId()), registration, lifetime);
    }

    public Optional<PendingRegistration> findPending(UUID registrationId) {
        return get(pendingKey(registrationId), PendingRegistration.class);
    }

    public void saveOtp(UUID registrationId, OtpState otp, Duration lifetime) {
        put(otpKey(registrationId), otp, lifetime);
    }

    public Optional<OtpState> findOtp(UUID registrationId) {
        return get(otpKey(registrationId), OtpState.class);
    }

    public void deleteOtp(UUID registrationId) {
        redis.delete(otpKey(registrationId));
    }

    public void deletePending(UUID registrationId) {
        redis.delete(pendingKey(registrationId));
    }

    public Duration otpTimeToLive(UUID registrationId) {
        long seconds = redis.getExpire(otpKey(registrationId));
        return Duration.ofSeconds(Math.max(seconds, 1));
    }

    public void markCompleted(UUID registrationId, Duration lifetime) {
        redis.opsForValue().set(completedKey(registrationId), "true", lifetime);
    }

    public boolean isCompleted(UUID registrationId) {
        return Boolean.TRUE.equals(redis.hasKey(completedKey(registrationId)));
    }

    public Duration pendingTimeToLive(UUID registrationId) {
        long seconds = redis.getExpire(pendingKey(registrationId));
        return Duration.ofSeconds(Math.max(seconds, 1));
    }

    public long incrementSendCount(String email, Duration window) {
        String key = sendCountKey(email);
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1) {
            redis.expire(key, window);
        }
        return count == null ? 0 : count;
    }

    private void put(String key, Object value, Duration lifetime) {
        try {
            redis.opsForValue().set(key, objectMapper.writeValueAsString(value), lifetime);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not store registration data.", exception);
        }
    }

    private <T> Optional<T> get(String key, Class<T> type) {
        String value = redis.opsForValue().get(key);
        if (value == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readValue(value, type));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not read registration data.", exception);
        }
    }

    private String pendingKey(UUID registrationId) {
        return PENDING_PREFIX + registrationId;
    }

    private String otpKey(UUID registrationId) {
        return OTP_PREFIX + registrationId;
    }

    private String completedKey(UUID registrationId) {
        return COMPLETED_PREFIX + registrationId;
    }

    private String sendCountKey(String email) {
        return SEND_COUNT_PREFIX + email;
    }
}