package com.example.warehouse.controller;

import com.example.warehouse.payload.response.ApiResponse;
import com.example.warehouse.payload.response.UserResponse;
import com.example.warehouse.security.SecurityUser;
import com.example.warehouse.service.UserService;
import com.example.warehouse.utility.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles endpoints related to the currently authenticated user's profile.
 */
@RestController
@RequestMapping("/me") // A common and clean endpoint for the current user
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    /**
     * Retrieves the profile information of the currently authenticated user.
     *
     * @param currentUser The authenticated user principal injected by Spring Security.
     * @return A ResponseEntity containing the user's profile data.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()") // Ensures only logged-in users can access this
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUserProfile(
            @AuthenticationPrincipal SecurityUser currentUser) {

        // The @AuthenticationPrincipal annotation provides the currently logged-in user.
        // We then use their username to fetch the full, updated details from the service.
        UserResponse userProfile = userService.getUserByUsername(currentUser.getUsername());

        return ResponseUtil.createSuccessResponse("User profile retrieved successfully", userProfile);
    }
}
