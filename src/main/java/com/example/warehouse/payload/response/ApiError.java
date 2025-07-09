package com.example.warehouse.payload.response;

import com.example.warehouse.enums.ApiErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

@Value // Using @Value for immutability
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Ignore null fields in JSON
public class ApiError {
    /**
     * Error code for machine to read and process.
     */
    ApiErrorCode code;

    /**
     * More detailed information about the error, often used for debugging.
     */
    Object details;
}
