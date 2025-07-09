package com.example.warehouse.payload.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * A standardized generic wrapper for all API responses.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;

    // Fields for a successful response
    private String message;
    private T data;

    // Fields for a failed response
    private String title;
    private ApiError error;

    // Private constructor for a successful response
    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Private constructor for a failed response
    private ApiResponse(boolean success, String title, ApiError error) {
        this.success = success;
        this.title = title;
        this.error = error;
    }

    /**
     * Factory method for creating a successful response.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Factory method for creating a failed response.
     */
    public static <T> ApiResponse<T> error(String title, ApiError error) {
        return new ApiResponse<>(false, title, error);
    }
}
