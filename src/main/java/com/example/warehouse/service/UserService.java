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
    private final AuditLogService auditLogService;
    private final SecurityContextService securityContextService;


    /**
     * Retrieves all users and maps them to a paginated response DTO.
     *
     * @param pageable Pagination information.
     * @return A paginated list of UserResponse DTOs.
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
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
        User actor = securityContextService.getCurrentActor(); // Get the logged-in user who is performing the action

        // Log the user creation action
        auditLogService.logAction(
                actor, // The actor is the admin who performed the action
                AuditAction.CREATE_USER,
                "users",
                savedUser.getId().toString(),
                String.format("User '%s' created new user '%s'", actor != null ? actor.getUsername() : "SYSTEM", savedUser.getUsername())
        );

        return userMapper.toUserResponse(savedUser);
    }

    @Transactional
    public UserResponse updateUser(Integer userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

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
        User actor = securityContextService.getCurrentActor(); // Get the logged-in user who is performing the action

        // Log the user update action
        auditLogService.logAction(
                actor, // The actor is the admin who performed the action
                AuditAction.UPDATE_USER,
                "users",
                updatedUser.getId().toString(),
                String.format("User '%s' updated user '%s'", actor != null ? actor.getUsername() : "SYSTEM", updatedUser.getUsername())
        );

        return userMapper.toUserResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(Integer userId) {
        User userToDelete = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        User actor = securityContextService.getCurrentActor(); // Get the actor before deleting the user

        // Log the user deletion action
        auditLogService.logAction(
                actor, // The actor is the admin who performed the action
                AuditAction.DELETE_USER,
                "users",
                userId.toString(),
                String.format("User '%s' deleted user '%s'", actor != null ? actor.getUsername() : "SYSTEM", userToDelete.getUsername())
        );

        userRepository.delete(userToDelete);
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Incorrect current password provided.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Log the password change action
        auditLogService.logAction(
                user, // In this case, the actor IS the user being changed
                AuditAction.CHANGE_PASSWORD,
                "users",
                user.getId().toString(),
                String.format("User '%s' changed their own password.", user.getUsername())
        );
    }

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
