package com.example.warehouse.controller;

import com.example.warehouse.payload.request.ChangePasswordRequest;
import com.example.warehouse.payload.response.ApiResponse;
import com.example.warehouse.payload.response.UserResponse;
import com.example.warehouse.security.SecurityUser;
import com.example.warehouse.service.UserService;
import com.example.warehouse.utility.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Handles endpoints related to the currently authenticated user's profile.
 */
@RestController
@RequestMapping("/me") // A common and clean endpoint for the current user
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    private final UserService userService;

    /**
     * Retrieves the profile information of the currently authenticated user.
     *
     * @param currentUser The authenticated user principal injected by Spring Security.
     * @return A ResponseEntity containing the user's profile data.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUserProfile(
            @AuthenticationPrincipal SecurityUser currentUser) {

        // The @AuthenticationPrincipal annotation provides the currently logged-in user.
        // We then use their username to fetch the full, updated details from the service.
        UserResponse userProfile = userService.getUserByUsername(currentUser.getUsername());

        return ResponseUtil.createSuccessResponse("User profile retrieved successfully", userProfile);
    }

    /**
     * Allows the authenticated user to change their own password.
     *
     * @param currentUser The authenticated user principal.
     * @param request     The request body containing password details.
     * @return A success response.
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal SecurityUser currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.changePassword(currentUser.getUsername(), request);

        return ResponseUtil.createSuccessResponse("Password changed successfully", null);
    }
}
