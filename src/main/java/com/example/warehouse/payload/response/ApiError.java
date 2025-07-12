package com.example.warehouse.payload.response;

import com.example.warehouse.enums.ErrorCodeEnum;
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
    private final ErrorCodeEnum errorCodeEnum;
    private final String message; // The detailed, human-readable error message
    private final String path;
    private Map<String, String> validationErrors;

    /**
     * Main constructor for API errors.
     */
    public ApiError(ErrorCodeEnum errorCodeEnum, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.errorCodeEnum = errorCodeEnum;
        this.message = message;
        this.path = path;
    }

    /**
     * Constructor for API errors that include validation failures.
     */
    public ApiError(ErrorCodeEnum errorCodeEnum, String message, String path, Map<String, String> validationErrors) {
        this(errorCodeEnum, message, path);
        this.validationErrors = validationErrors;
    }
}
