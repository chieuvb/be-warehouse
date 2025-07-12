package com.example.warehouse.utility;

import com.example.warehouse.enums.ErrorCodeEnum;
import com.example.warehouse.payload.response.ApiError;
import com.example.warehouse.payload.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public final class ResponseUtil {

    private ResponseUtil() {}

    public static <T> ResponseEntity<ApiResponse<T>> createSuccessResponse(String message, T data) {
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }

    public static ResponseEntity<ApiResponse<Object>> createErrorResponse(
            HttpStatus status, ErrorCodeEnum errorCodeEnum, String message, String path, Map<String, String> validationErrors) {

        ApiError apiError = new ApiError(errorCodeEnum, message, path, validationErrors);
        String title = status.getReasonPhrase(); // e.g., "Not Found", "Conflict"
        return new ResponseEntity<>(ApiResponse.error(title, apiError), status);
    }

    public static ResponseEntity<ApiResponse<Object>> createErrorResponse(HttpStatus status, ErrorCodeEnum errorCodeEnum, String message, String path) {
        return createErrorResponse(status, errorCodeEnum, message, path, null);
    }
}
