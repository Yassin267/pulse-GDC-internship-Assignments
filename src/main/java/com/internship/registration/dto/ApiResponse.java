package com.internship.registration.dto;

public record ApiResponse<T>(T result, ApiError error) {

    public static <T> ApiResponse<T> success(T result) {
        return new ApiResponse<>(result, null);
    }

    public static <T> ApiResponse<T> failure(ApiError error) {
        return new ApiResponse<>(null, error);
    }
}