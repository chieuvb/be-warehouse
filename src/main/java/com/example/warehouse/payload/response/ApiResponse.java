package com.example.warehouse.payload.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

/**
 * A generic and standardized API response wrapper.
 *
 * @param <T> The type of the data payload.
 */
@Value // Use @Value for immutability
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Exclude null fields from JSON output
public class ApiResponse<T> {
    /**
     * Indicates if the request was successful.
     */
    boolean success;

    /**
     * A human-readable message providing details about the outcome.
     */
    String message;

    /**
     * The actual data payload of the response. Null if the request failed.
     */
    T data;

    /**
     * Detailed error information. Null if the request was successful.
     */
    ApiError error;
}
