package com.example.warehouse.payload.response;

import com.example.warehouse.enums.ErrorCode;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private final LocalDateTime timestamp;
    private final ErrorCode errorCode;
    private final String message; // The detailed, human-readable error message
    private final String path;
    private Map<String, String> validationErrors;

    /**
     * Main constructor for API errors.
     */
    public ApiError(ErrorCode errorCode, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
    }

    /**
     * Constructor for API errors that include validation failures.
     */
    public ApiError(ErrorCode errorCode, String message, String path, Map<String, String> validationErrors) {
        this(errorCode, message, path);
        this.validationErrors = validationErrors;
    }
}
