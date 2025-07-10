package com.example.warehouse.service;

import com.example.warehouse.entity.Role;
import com.example.warehouse.entity.User;
import com.example.warehouse.enums.AuditAction;
import com.example.warehouse.exception.ResourceConflictException;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.mapper.UserMapper;
import com.example.warehouse.payload.request.ChangePasswordRequest;
import com.example.warehouse.payload.request.UserCreateRequest;
import com.example.warehouse.payload.request.UserUpdateRequest;
import com.example.warehouse.payload.response.UserResponse;
import com.example.warehouse.repository.RoleRepository;
import com.example.warehouse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Service class for managing users, including CRUD operations and password management.
 * It uses repositories for data access and a mapper for DTO conversions.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService; // Assuming you have an AuditLogService for logging actions

    /**
     * Retrieves all users and maps them to a paginated response DTO.
     *
     * @param pageable Pagination information.
     * @return A paginated list of UserResponse DTOs.
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        // Use the mapper for cleaner code
        return userRepository.findAll(pageable).map(userMapper::toUserResponse);
    }

    /**
     * Retrieves a user by their ID and maps them to a response DTO.
     *
     * @param userId The ID of the user to retrieve.
     * @return A UserResponse DTO of the found user.
     * @throws ResourceNotFoundException if no user is found with the given ID.
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        // Use the mapper
        return userMapper.toUserResponse(user);
    }

    /**
     * Finds a user by their username and maps them to a response DTO.
     *
     * @param username The username to search for.
     * @return A UserResponse DTO of the found user.
     * @throws ResourceNotFoundException if no user is found with the given username.
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return userMapper.toUserResponse(user);
    }

    /**
     * Creates a new user with the provided details.
     *
     * @param request The request containing user details.
     * @return A UserResponse DTO of the created user.
     * @throws ResourceConflictException if a user with the same username or email already exists.
     */
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceConflictException("User", "username", request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceConflictException("User", "email", request.getEmail());
        }

        Set<Role> roles = findRoles(request.getRoles());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .isActive(true)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);

        // Log the user creation action
        auditLogService.logAction(
                savedUser, // The actor is the newly created user
                AuditAction.CREATE_USER,
                "users",
                savedUser.getId().toString(),
                "User created successfully."
        );

        return userMapper.toUserResponse(savedUser);
    }

    /**
     * Updates an existing user's details.
     *
     * @param userId  The ID of the user to update.
     * @param request The request containing updated user details.
     * @return A UserResponse DTO of the updated user.
     * @throws ResourceNotFoundException if no user is found with the given ID.
     * @throws ResourceConflictException if the email is already in use by another user.
     */
    @Transactional
    public UserResponse updateUser(Integer userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check for email conflict with other users
        userRepository.findByEmail(request.getEmail()).ifPresent(existingUser -> {
            if (!existingUser.getId().equals(userId)) {
                throw new ResourceConflictException("User", "email", request.getEmail());
            }
        });

        Set<Role> roles = findRoles(request.getRoles());

        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setIsActive(request.getIsActive());
        user.setRoles(roles);

        User updatedUser = userRepository.save(user);

        // Log the user update action
        auditLogService.logAction(
                updatedUser, // The actor is the updated user
                AuditAction.UPDATE_USER,
                "users",
                updatedUser.getId().toString(),
                "User updated successfully."
        );

        return userMapper.toUserResponse(updatedUser);
    }

    /**
     * Deletes a user by their ID.
     *
     * @param userId The ID of the user to delete.
     * @throws ResourceNotFoundException if no user is found with the given ID.
     */
    @Transactional
    public void deleteUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        // Log the user deletion action
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        auditLogService.logAction(
                user, // The actor is the user being deleted
                AuditAction.DELETE_USER,
                "users",
                userId.toString(),
                "User deleted successfully."
        );

        // Consider business logic: should you be able to delete your own account?
        // Or the last admin account? For now, we allow deletion.
        userRepository.deleteById(userId);
    }

    /**
     * Changes the password for the specified user after verifying their current password.
     *
     * @param username The username of the user changing their password.
     * @param request  The request containing the current and new passwords.
     * @throws BadCredentialsException if the current password is incorrect.
     */
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        // 1. Find the user by their username (from the security context)
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        // 2. Verify that the provided current password matches the stored password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Incorrect current password provided.");
        }

        // 3. Encode the new password and update the user entity
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));

        // 4. Log the password change action
        auditLogService.logAction(
                user, // The actor is the user changing their password
                AuditAction.CHANGE_PASSWORD,
                "users",
                user.getId().toString(),
                "User changed their password successfully."
        );

        // 5. Save the updated user
        userRepository.save(user);
    }

    /**
     * Finds roles by their names and returns a set of Role entities.
     *
     * @param roleNames The set of role names to find.
     * @return A set of Role entities corresponding to the provided names.
     * @throws ResourceNotFoundException if any role name does not exist in the database.
     */
    private Set<Role> findRoles(Set<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
            roles.add(role);
        }
        return roles;
    }
}
