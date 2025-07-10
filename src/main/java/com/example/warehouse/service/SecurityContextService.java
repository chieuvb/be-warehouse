package com.example.warehouse.service;

import com.example.warehouse.entity.User;
import com.example.warehouse.security.SecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * A service dedicated to interacting with the Spring Security context
 * to retrieve information about the currently authenticated user.
 */
@Service
public class SecurityContextService {

    /**
     * Retrieves the currently authenticated user from the SecurityContext.
     * This is the "actor" for audit logging purposes.
     *
     * @return The authenticated User entity, or null if no user is authenticated or the principal is of an unexpected type.
     */
    public User getCurrentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check for null, unauthenticated, or anonymous users
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof SecurityUser) {
            // Our custom SecurityUser directly holds the User entity
            return ((SecurityUser) principal).user();
        }

        // Fallback for other principal types or system processes
        return null;
    }
}
