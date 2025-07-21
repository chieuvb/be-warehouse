package com.example.warehouse.enums;

public enum ErrorCodeEnum {

    // Authentication & Authorization Errors
    AUTH_INVALID_CREDENTIALS,
    AUTH_TOKEN_EXPIRED,
    AUTH_TOKEN_MALFORMED,
    AUTH_TOKEN_INVALID,
    AUTH_ACCESS_DENIED,
    FORBIDDEN_OPERATION,
    UNAUTHORIZED_ACCESS,

    // User-related Errors
    USER_NOT_FOUND,
    USERNAME_EXISTS,
    EMAIL_EXISTS,
    USER_LOCKED,
    USER_INACTIVE,

    // Input Validation Errors
    INVALID_INPUT,
    PASSWORD_WEAK,
    FIELD_REQUIRED,
    INVALID_EMAIL_FORMAT,
    VALIDATION_FAILED,

    // Data & Resource Errors
    RESOURCE_NOT_FOUND,
    DATA_CONFLICT,
    DUPLICATE_ENTRY,

    // General System Errors
    INTERNAL_ERROR,
    SERVICE_UNAVAILABLE,
    DATABASE_ERROR,
    TIMEOUT_ERROR,
    UNEXPECTED_ERROR,
    GENERAL_ERROR
}
