package com.example.warehouse.service;

import com.example.warehouse.entity.Role;
import com.example.warehouse.entity.User;
import com.example.warehouse.enums.AuditAction;
import com.example.warehouse.exception.ResourceConflictException;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.mapper.UserMapper;
import com.example.warehouse.payload.request.ChangePasswordRequest;
import com.example.warehouse.payload.request.UpdateUserRolesRequest;
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

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final SecurityContextService securityContextService;

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toUserResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Integer userId) {
        return userRepository.findById(userId)
                .map(userMapper::toUserResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toUserResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    /**
     * Creates a new user with the provided details.
     * <p>
     * **Critical Safeguard**: Ensures that the username and email are unique.
     *
     * @param request The request containing user details.
     * @return The created UserResponse.
     * @throws ResourceConflictException if the username or email already exists.
     */
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceConflictException("User", "username", request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceConflictException("User", "email", request.getEmail());
        }

        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role 'ROLE_USER' not found."));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .isActive(true)
                .roles(Set.of(defaultRole))
                .build();

        User savedUser = userRepository.save(user);

        auditLogService.logAction(
                securityContextService.getCurrentActor(),
                AuditAction.CREATE_USER,
                "users",
                savedUser.getId().toString(),
                String.format("Created user '%s'", savedUser.getUsername())
        );

        return userMapper.toUserResponse(savedUser);
    }

    /**
     * Updates an existing user's details.
     * <p>
     * **Critical Safeguard**: Checks for email conflict if the email is being changed.
     *
     * @param userId The ID of the user to update.
     * @param request The request containing updated user details.
     * @return The updated UserResponse.
     * @throws ResourceNotFoundException if the user does not exist.
     * @throws ResourceConflictException if the new email already exists.
     */
    @Transactional
    public UserResponse updateUser(Integer userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check for email conflict if the email is being changed
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceConflictException("User", "email", request.getEmail());
        }

        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setIsActive(request.getIsActive());

        User updatedUser = userRepository.save(user);

        auditLogService.logAction(
                securityContextService.getCurrentActor(),
                AuditAction.UPDATE_USER,
                "users",
                updatedUser.getId().toString(),
                String.format("Updated user details for '%s'", updatedUser.getUsername())
        );

        return userMapper.toUserResponse(updatedUser);
    }

    /** Updates the roles of a user.
     * <p>
     * **Critical Safeguard**: Prevents assigning roles that do not exist.
     *
     * @param userId The ID of the user whose roles are to be updated.
     * @param request The request containing the new role IDs.
     * @throws ResourceNotFoundException if the user or any role does not exist.
     */
    @Transactional
    public void updateUserRoles(Integer userId, UpdateUserRolesRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Set<Role> newRoles = new HashSet<>(roleRepository.findAllById(request.getRoleIds()));
        if (newRoles.size() != request.getRoleIds().size()) {
            throw new ResourceNotFoundException("One or more roles not found.");
        }

        user.setRoles(newRoles);
        userRepository.save(user);

        auditLogService.logAction(
                securityContextService.getCurrentActor(),
                AuditAction.UPDATE_USER, // Can be a more specific action if you add one
                "user_roles",
                user.getId().toString(),
                String.format("Updated roles for user '%s'", user.getUsername())
        );
    }

    /**
     * Changes the password for a user.
     * <p>
     * **Critical Safeguard**: Validates the current password before allowing a change.
     *
     * @param username The username of the user whose password is to be changed.
     * @param request The request containing the current and new passwords.
     * @throws ResourceNotFoundException if the user does not exist.
     * @throws BadCredentialsException if the current password is incorrect.
     */
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

    /**
     * Deletes a user by their ID.
     * <p>
     * **Critical Safeguard**: Prevents deletion of the last admin user.
     *
     * @param userId The ID of the user to delete.
     * @throws ResourceNotFoundException if the user does not exist.
     * @throws ResourceConflictException if attempting to delete the last admin user.
     */
    @Transactional
    public void deleteUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // **Critical Safeguard**: Prevent deletion of the last admin user.
        boolean isDeletingAdmin = user.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getName()));
        if (isDeletingAdmin) {
            long adminCount = userRepository.countByRoles_Name("ROLE_ADMIN");
            if (adminCount <= 1) {
                throw new ResourceConflictException("Cannot delete the last administrator account.");
            }
        }

        auditLogService.logAction(
                securityContextService.getCurrentActor(),
                AuditAction.DELETE_USER,
                "users",
                userId.toString(),
                String.format("Deleted user '%s'", user.getUsername())
        );

        userRepository.delete(user);
    }
}
