package com.example.warehouse.exception;

import com.example.warehouse.enums.ErrorCodeEnum;
import com.example.warehouse.payload.response.ApiError;
import com.example.warehouse.payload.response.ApiResponse;
import com.example.warehouse.utility.ResponseUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.stream.Collectors;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({ResourceNotFoundException.class})
    protected ResponseEntity<ApiResponse<Object>> handleResourceNotFound(RuntimeException ex, WebRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseUtil.createErrorResponse(HttpStatus.NOT_FOUND, ErrorCodeEnum.RESOURCE_NOT_FOUND, ex.getMessage(), getRequestPath(request));
    }

    @ExceptionHandler({ResourceConflictException.class})
    protected ResponseEntity<ApiResponse<Object>> handleResourceConflict(RuntimeException ex, WebRequest request) {
        log.warn("Data conflict: {}", ex.getMessage());
        return ResponseUtil.createErrorResponse(HttpStatus.CONFLICT, ErrorCodeEnum.DATA_CONFLICT, ex.getMessage(), getRequestPath(request));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(WebRequest request) {
        log.warn("Access Denied: User attempted to access a protected resource. Path: {}", getRequestPath(request));
        String message = "You do not have permission to perform this action.";
        return ResponseUtil.createErrorResponse(HttpStatus.FORBIDDEN, ErrorCodeEnum.FORBIDDEN_OPERATION, message, getRequestPath(request));
    }

    /**
     * Handles authentication failures, such as bad credentials during login
     * or incorrect password during a password change.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        String path = getRequestPath(request);
        log.warn("Authentication failure for path {}: {}", path, ex.getMessage());

        // Default message for login attempts
        String message = "Invalid username or password.";
        ErrorCodeEnum errorCodeEnum = ErrorCodeEnum.AUTH_INVALID_CREDENTIALS;
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        // Provide a more specific response if it's a password change attempt
        if (ex.getMessage().contains("current password")) {
            message = ex.getMessage();
            status = HttpStatus.BAD_REQUEST; // Use 400 for a bad request, not 401
        }

        return ResponseUtil.createErrorResponse(status, errorCodeEnum, message, path);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<Object>> handleExpiredJwt(
            ExpiredJwtException ex, HttpServletRequest request) {

        return ResponseUtil.createErrorResponse(
                HttpStatus.UNAUTHORIZED,
                ErrorCodeEnum.AUTH_TOKEN_EXPIRED,
                "Your session has expired. Please login again.",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<ApiResponse<Object>> handleMalformedJwt(
            MalformedJwtException ex, HttpServletRequest request) {

        return ResponseUtil.createErrorResponse(
                HttpStatus.UNAUTHORIZED,
                ErrorCodeEnum.AUTH_TOKEN_MALFORMED,
                "Invalid token format.",
                request.getRequestURI()
        );
    }

    @ExceptionHandler({SignatureException.class, SecurityException.class})
    public ResponseEntity<ApiResponse<Object>> handleJwtSignature(
            Exception ex, HttpServletRequest request) {

        return ResponseUtil.createErrorResponse(
                HttpStatus.UNAUTHORIZED,
                ErrorCodeEnum.AUTH_TOKEN_INVALID,
                "Invalid token signature.",
                request.getRequestURI()
        );
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        Map<String, String> validationErrors = ex.getBindingResult().getAllErrors().stream()
                .collect(Collectors.toMap(
                        error -> ((FieldError) error).getField(),
                        error -> error.getDefaultMessage() == null ? "Invalid value" : error.getDefaultMessage()
                ));

        String path = getRequestPath(request);
        String message = "Input validation failed";
        log.warn("Validation error on path {}: {}", path, validationErrors);

        HttpStatus httpStatus = (HttpStatus) status;
        ApiError apiError = new ApiError(ErrorCodeEnum.VALIDATION_FAILED, message, path, validationErrors);
        ApiResponse<Object> apiResponse = ApiResponse.error(httpStatus.getReasonPhrase(), apiError);

        return new ResponseEntity<>(apiResponse, headers, status);
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ApiResponse<Object>> handleAll(Exception ex, WebRequest request) {
        String path = getRequestPath(request);
        log.error("An unexpected error occurred on path {}:", path, ex);
        String message = "An unexpected error occurred. Please contact support.";
        return ResponseUtil.createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodeEnum.INTERNAL_ERROR, message, path);
    }

    private String getRequestPath(WebRequest request) {
        return ((ServletWebRequest) request).getRequest().getRequestURI();
    }
}
