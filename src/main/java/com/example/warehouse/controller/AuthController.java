package com.example.warehouse.controller;

import com.example.warehouse.payload.request.LoginRequest;
import com.example.warehouse.payload.request.RegisterRequest;
import com.example.warehouse.payload.response.ApiResponse;
import com.example.warehouse.payload.response.AuthResponse;
import com.example.warehouse.payload.response.UserResponse;
import com.example.warehouse.service.AuthService;
import com.example.warehouse.utility.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.login(loginRequest);
        return ResponseUtil.createSuccessResponse("User logged in successfully!", authResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        AuthResponse newUser = authService.register(registerRequest);
        // Using a 201 Created status for resource creation is a REST best practice
        ApiResponse<AuthResponse> response = ApiResponse.success("User registered successfully!", newUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
