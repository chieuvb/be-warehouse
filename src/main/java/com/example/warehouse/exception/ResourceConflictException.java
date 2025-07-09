package com.example.warehouse.exception;

public class ResourceConflictException extends RuntimeException {
    public ResourceConflictException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue));
    }

    public ResourceConflictException(String message) {
        super(message);
    }
}