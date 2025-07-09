package com.example.warehouse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an action would result in a data conflict (e.g., duplicate unique key).
 * Results in an HTTP 409 Conflict response.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ResourceConflictException extends RuntimeException {

    public ResourceConflictException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s with %s '%s' already exists.", resourceName, fieldName, fieldValue));
    }

    public ResourceConflictException(String message) {
        super(message);
    }
}
