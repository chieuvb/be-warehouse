package com.example.warehouse.service;

import com.example.warehouse.entity.Role;
import com.example.warehouse.entity.User;
import com.example.warehouse.enums.AuditAction;
import com.example.warehouse.exception.ResourceConflictException;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.payload.request.LoginRequest;
import com.example.warehouse.payload.request.RegisterRequest;
import com.example.warehouse.payload.response.AuthResponse;
import com.example.warehouse.repository.RoleRepository;
import com.example.warehouse.repository.UserRepository;
import com.example.warehouse.security.JwtService;
import com.example.warehouse.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService; // Injected the AuditLogService

    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        // 1. Authenticate the user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // 2. Set the authentication context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Generate the JWT
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        String jwtToken = jwtService.generateToken(securityUser);

        // 4. Log the successful login action
        auditLogService.logAction(
                securityUser.user(), // Get the actor directly from the principal
                AuditAction.USER_LOGIN_SUCCESS,
                "users",
                securityUser.user().getId().toString(),
                "User logged in successfully."
        );

        return new AuthResponse(jwtToken);
    }

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        // 1. Check for username/email conflicts
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new ResourceConflictException("User", "username", registerRequest.getUsername());
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ResourceConflictException("User", "email", registerRequest.getEmail());
        }

        // 2. Find the default role for the new user
        Role userRole = roleRepository.findByName("ROLE_VIEWER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role 'ROLE_USER' not found. Please configure the database."));

        // 3. Create and save the new user entity
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .fullName(registerRequest.getFullName())
                .isActive(true)
                .roles(Set.of(userRole))
                .build();
        User savedUser = userRepository.save(user);

        // 4. Log the registration action
        auditLogService.logAction(
                savedUser, // The actor is the newly created user
                AuditAction.CREATE_USER,
                "users",
                savedUser.getId().toString(),
                "New user registered."
        );

        // 5. Authenticate the new user and generate a token without a second login call
        SecurityUser securityUser = new SecurityUser(savedUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                securityUser,
                null, // Credentials are not needed post-registration
                securityUser.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwtToken = jwtService.generateToken(securityUser);
        return new AuthResponse(jwtToken);
    }
}
