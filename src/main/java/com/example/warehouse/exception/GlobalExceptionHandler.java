package com.example.warehouse.exception;

import com.example.warehouse.payload.response.ApiError;
import com.example.warehouse.payload.response.ApiResponse;
import com.example.warehouse.enums.ApiErrorCode;
import com.example.warehouse.utility.ResponseUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Xử lý các lỗi xác thực của Spring Security
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());

        ApiError apiError = ApiError.builder()
                .code(ApiErrorCode.AUTH_INVALID_CREDENTIALS)
                .details(ex.getMessage())
                .build();

        ApiResponse<Object> apiResponse = ResponseUtil.error("Invalid username or password", apiError);

        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED); // 401
    }

    // Xử lý lỗi người dùng không tồn tại
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserNotFoundException(UserNotFoundException ex, WebRequest request) {
        log.warn("User not found: {}", ex.getMessage());

        ApiError apiError = ApiError.builder()
                .code(ApiErrorCode.USER_NOT_FOUND)
                .details(ex.getMessage())
                .build();

        ApiResponse<Object> apiResponse = ResponseUtil.error("User not found", apiError);

        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND); // 404
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ApiResponse<Object>> handleConflict(ResourceConflictException ex) {
        log.warn("Resource conflict: {}", ex.getMessage());

        ApiError apiError = ApiError.builder()
                .code(ApiErrorCode.DATA_CONFLICT)
                .details(ex.getMessage())
                .build();

        ApiResponse<Object> apiResponse = ResponseUtil.error("Resource conflict", apiError);

        return new ResponseEntity<>(apiResponse, HttpStatus.CONFLICT); // 409
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());

        ApiError apiError = ApiError.builder()
                .code(ApiErrorCode.RESOURCE_NOT_FOUND)
                .details(ex.getMessage())
                .build();

        ApiResponse<Object> apiResponse = ResponseUtil.error("The requested resource was not found", apiError);

        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    // Xử lý các lỗi chung, không mong muốn
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(Exception ex, WebRequest request) {
        log.error("An unexpected error occurred: ", ex);

        ApiError apiError = ApiError.builder()
                .code(ApiErrorCode.INTERNAL_ERROR)
                .details("An unexpected internal server error occurred.")
                .build();

        ApiResponse<Object> apiResponse = ResponseUtil.error("An internal error occurred. Please try again later.", apiError);

        return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR); // 500
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        // Lấy các lỗi validation và format lại
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiError apiError = ApiError.builder()
                .code(ApiErrorCode.VALIDATION_FAILED) // Thêm mã lỗi này vào enum
                .details(errors) // Trả về chi tiết lỗi của từng field
                .build();

        ApiResponse<Object> apiResponse = ResponseUtil.error("Validation failed", apiError);

        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST); // 400
    }
}