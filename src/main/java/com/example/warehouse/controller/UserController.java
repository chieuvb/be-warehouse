package com.example.warehouse.controller;

import com.example.warehouse.payload.request.UserCreateRequest;
import com.example.warehouse.payload.request.UserUpdateRequest;
import com.example.warehouse.payload.response.ApiResponse;
import com.example.warehouse.payload.response.UserResponse;
import com.example.warehouse.service.UserService;
import com.example.warehouse.utility.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing users in the warehouse management system.
 * Provides endpoints for user management operations such as creating, updating, retrieving, and deleting users.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Secure all endpoints in this controller
public class UserController {

    private final UserService userService;

    /**
     * Retrieves a paginated list of all users.
     *
     * @param pageable Pagination information.
     * @return A ResponseEntity containing a paginated list of UserResponse DTOs.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @PageableDefault(sort = "fullName") Pageable pageable) {
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseUtil.createSuccessResponse("Users retrieved successfully", users);
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userId The ID of the user to retrieve.
     * @return A ResponseEntity containing the UserResponse DTO of the found user.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable("id") Integer userId) {
        UserResponse user = userService.getUserById(userId);
        return ResponseUtil.createSuccessResponse("User retrieved successfully", user);
    }

    /**
     * Creates a new user.
     *
     * @param request The request body containing user creation details.
     * @return A ResponseEntity containing the created UserResponse DTO.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse newUser = userService.createUser(request);
        ApiResponse<UserResponse> response = ApiResponse.success("User created successfully", newUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Updates an existing user.
     *
     * @param userId  The ID of the user to update.
     * @param request The request body containing updated user details.
     * @return A ResponseEntity containing the updated UserResponse DTO.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable("id") Integer userId,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse updatedUser = userService.updateUser(userId, request);
        return ResponseUtil.createSuccessResponse("User updated successfully", updatedUser);
    }

    /**
     * Deletes a user by their ID.
     *
     * @param userId The ID of the user to delete.
     * @return A ResponseEntity indicating the deletion was successful.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable("id") Integer userId) {
        userService.deleteUser(userId);
        return ResponseUtil.createSuccessResponse("User deleted successfully", null);
    }
}
