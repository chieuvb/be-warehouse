package com.example.warehouse.service;

import com.example.warehouse.entity.Role;
import com.example.warehouse.entity.User;
import com.example.warehouse.exception.ResourceConflictException;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.payload.request.LoginRequest;
import com.example.warehouse.payload.request.RegisterRequest;
import com.example.warehouse.payload.response.AuthResponse;
import com.example.warehouse.payload.response.UserResponse;
import com.example.warehouse.repository.RoleRepository;
import com.example.warehouse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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

    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwtToken = jwtService.generateToken(userDetails);

        return new AuthResponse(jwtToken);
    }

    @Transactional
    public UserResponse register(RegisterRequest registerRequest) {
        // 1. Check if username already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new ResourceConflictException("User", "username", registerRequest.getUsername());
        }

        // 2. Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ResourceConflictException("User", "email", registerRequest.getEmail());
        }

        // 3. Find the default role for new users
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role 'ROLE_USER' not found. Please configure the database."));

        // 4. Create a new user entity
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .fullName(registerRequest.getFullName())
                .isActive(true)
                .roles(Set.of(userRole))
                .build();

        // 5. Save the user to the database
        User savedUser = userRepository.save(user);

        // 6. Map the saved user to a safe response DTO
        return UserResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .build();
    }
}
