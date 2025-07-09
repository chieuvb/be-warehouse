package com.example.warehouse.enums;

public enum ApiErrorCode {

    // ✅ Lỗi xác thực
    AUTH_INVALID_CREDENTIALS,
    AUTH_TOKEN_EXPIRED,
    AUTH_TOKEN_INVALID,
    AUTH_ACCESS_DENIED,

    // ✅ Lỗi người dùng
    USER_NOT_FOUND,
    USERNAME_EXISTS,
    EMAIL_EXISTS,
    USER_LOCKED,
    USER_INACTIVE,

    // ✅ Lỗi đăng ký/đầu vào
    INVALID_INPUT,
    PASSWORD_WEAK,
    FIELD_REQUIRED,
    INVALID_EMAIL_FORMAT,

    // ✅ Lỗi hệ thống
    INTERNAL_ERROR,
    SERVICE_UNAVAILABLE,
    DATABASE_ERROR,
    TIMEOUT_ERROR,

    // ✅ Lỗi quyền hạn
    FORBIDDEN_OPERATION,
    UNAUTHORIZED_ACCESS,

    // ✅ Lỗi dữ liệu
    DATA_NOT_FOUND,
    DATA_CONFLICT,
    DUPLICATE_ENTRY,
    VALIDATION_FAILED,
    UNEXPECTED_ERROR,
    RESOURCE_NOT_FOUND, GENERAL_ERROR
}
