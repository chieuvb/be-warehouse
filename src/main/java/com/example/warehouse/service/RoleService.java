package com.example.warehouse.service;

import com.example.warehouse.entity.Role;
import com.example.warehouse.enums.AuditAction;
import com.example.warehouse.exception.ResourceConflictException;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.mapper.RoleMapper;
import com.example.warehouse.payload.request.RoleRequest;
import com.example.warehouse.payload.response.RoleResponse;
import com.example.warehouse.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing roles in the warehouse management system.
 * Provides methods to create, update, delete, and retrieve roles.
 */
@Service
@RequiredArgsConstructor
public class RoleService {

    private static final Set<String> PROTECTED_ROLES = Set.of("ROLE_ADMIN", "ROLE_USER");

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final AuditLogService auditLogService;
    private final SecurityContextService securityContextService;

    /**
     * Retrieves all roles in the system.
     *
     * @return A list of RoleResponse DTOs representing all roles.
     */
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toRoleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a role by its ID.
     *
     * @param roleId The ID of the role to retrieve.
     * @return A RoleResponse DTO representing the specified role.
     * @throws ResourceNotFoundException if the role with the given ID does not exist.
     */
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Integer roleId) {
        return roleRepository.findById(roleId)
                .map(roleMapper::toRoleResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));
    }

    /**
     * Creates a new role in the system.
     *
     * @param request The RoleRequest DTO containing the details of the role to create.
     * @return A RoleResponse DTO representing the created role.
     * @throws ResourceConflictException if a role with the same name already exists.
     */
    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new ResourceConflictException("Role", "name", request.getName());
        }

        Role role = Role.builder()
                .name(request.getName())
                .build();

        Role savedRole = roleRepository.save(role);

        auditLogService.logAction(
                securityContextService.getCurrentActor(),
                AuditAction.CREATE_ROLE,
                "roles",
                savedRole.getId().toString(),
                String.format("Created role '%s'", savedRole.getName())
        );

        return roleMapper.toRoleResponse(savedRole);
    }

    /**
     * Updates an existing role in the system.
     *
     * @param roleId  The ID of the role to update.
     * @param request The RoleRequest DTO containing the updated details of the role.
     * @return A RoleResponse DTO representing the updated role.
     * @throws ResourceNotFoundException if the role with the given ID does not exist.
     * @throws ResourceConflictException if a role with the same name already exists or if trying to modify a protected role.
     */
    @Transactional
    public RoleResponse updateRole(Integer roleId, RoleRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        if (PROTECTED_ROLES.contains(role.getName())) {
            throw new ResourceConflictException("Cannot modify a protected system role: " + role.getName());
        }

        roleRepository.findByName(request.getName()).ifPresent(existingRole -> {
            if (!existingRole.getId().equals(roleId)) {
                throw new ResourceConflictException("Role", "name", request.getName());
            }
        });

        role.setName(request.getName());
        Role updatedRole = roleRepository.save(role);

        auditLogService.logAction(
                securityContextService.getCurrentActor(),
                AuditAction.UPDATE_ROLE,
                "roles",
                updatedRole.getId().toString(),
                String.format("Updated role name to '%s'", updatedRole.getName())
        );

        return roleMapper.toRoleResponse(updatedRole);
    }

    /**
     * Deletes a role by its ID.
     *
     * @param roleId The ID of the role to delete.
     * @throws ResourceNotFoundException if the role with the given ID does not exist.
     * @throws ResourceConflictException if the role is protected or currently assigned to users.
     */
    @Transactional
    public void deleteRole(Integer roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        if (PROTECTED_ROLES.contains(role.getName())) {
            throw new ResourceConflictException("Cannot delete a protected system role: " + role.getName());
        }

        // Important: Prevent deletion if the role is assigned to any users.
        if (!role.getUsers().isEmpty()) {
            throw new ResourceConflictException("Cannot delete role '" + role.getName() + "' because it is currently assigned to " + role.getUsers().size() + " user(s).");
        }

        auditLogService.logAction(
                securityContextService.getCurrentActor(),
                AuditAction.DELETE_ROLE,
                "roles",
                roleId.toString(),
                String.format("Deleted role '%s'", role.getName())
        );

        roleRepository.delete(role);
    }
}
