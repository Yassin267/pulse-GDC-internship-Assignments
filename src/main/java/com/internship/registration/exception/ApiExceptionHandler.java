package com.internship.registration.exception;

import com.internship.registration.dto.ApiError;
import com.internship.registration.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> validation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));
        return failure(HttpStatus.BAD_REQUEST, 1001, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> badRequest(Exception exception) {
        return failure(HttpStatus.BAD_REQUEST, 1001, "The request is invalid.");
    }

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<ApiResponse<Void>> registrationFailure(RegistrationException exception) {
        return failure(exception.getStatus(), exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> methodNotAllowed(HttpRequestMethodNotSupportedException exception) {
        return failure(HttpStatus.METHOD_NOT_ALLOWED, 1001, "The HTTP method is not supported.");
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> notFound(NoHandlerFoundException exception) {
        return failure(HttpStatus.NOT_FOUND, 9001, "The requested resource was not found.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> unexpected(Exception exception) {
        return failure(HttpStatus.INTERNAL_SERVER_ERROR, 9001, "An unexpected error occurred.");
    }

    private ResponseEntity<ApiResponse<Void>> failure(HttpStatus status, int code, String message) {
        return ResponseEntity
                .status(status)
                .body(ApiResponse.failure(new ApiError(code, message)));
    }
}