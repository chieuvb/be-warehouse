package com.example.warehouse.utility;

import com.example.warehouse.payload.response.ApiError;
import com.example.warehouse.payload.response.ApiResponse;

public class ResponseUtil {

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, ApiError error) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .error(error)
                .build();
    }
}
