package com.example.warehouse.enums;

public enum ErrorCodeEnum {

    // Authentication & Authorization Errors
    AUTH_INVALID_CREDENTIALS,
    AUTH_TOKEN_EXPIRED,
    AUTH_TOKEN_MALFORMED,
    AUTH_TOKEN_INVALID,
    FORBIDDEN_OPERATION,
    UNAUTHORIZED_ACCESS,

    // User-related Errors
    USER_NOT_FOUND,

    // Input Validation Errors
    VALIDATION_FAILED,

    // Data & Resource Errors
    RESOURCE_NOT_FOUND,
    DATA_CONFLICT,

    // System Errors
    INTERNAL_ERROR
}
