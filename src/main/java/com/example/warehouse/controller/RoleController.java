package com.example.warehouse.controller;

import com.example.warehouse.payload.request.AssignUsersRequest;
import com.example.warehouse.payload.request.RoleRequest;
import com.example.warehouse.payload.response.ApiResponse;
import com.example.warehouse.payload.response.RoleResponse;
import com.example.warehouse.service.RoleService;
import com.example.warehouse.utility.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing roles in the warehouse management system.
 * Provides endpoints to create, update, delete, and retrieve roles.
 */
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
// Role management is a high-privilege operation, restricted to Admins.
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    private final RoleService roleService;

    /**
     * Retrieves all roles in the system.
     *
     * @return A ResponseEntity containing a list of RoleResponse DTOs.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        List<RoleResponse> roles = roleService.getAllRoles();
        return ResponseUtil.createSuccessResponse("Roles retrieved successfully", roles);
    }

    /**
     * Retrieves a role by its ID.
     *
     * @param id The ID of the role to retrieve.
     * @return A ResponseEntity containing the RoleResponse DTO for the specified role.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable Integer id) {
        RoleResponse role = roleService.getRoleById(id);
        return ResponseUtil.createSuccessResponse("Role retrieved successfully", role);
    }

    /**
     * Creates a new role in the system.
     *
     * @param request The RoleRequest DTO containing the details of the role to create.
     * @return A ResponseEntity containing the created RoleResponse DTO.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleRequest request) {
        RoleResponse newRole = roleService.createRole(request);
        ApiResponse<RoleResponse> response = ApiResponse.success("Role created successfully", newRole);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Updates an existing role in the system.
     *
     * @param id      The ID of the role to update.
     * @param request The RoleRequest DTO containing the updated details of the role.
     * @return A ResponseEntity containing the updated RoleResponse DTO.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable Integer id,
            @Valid @RequestBody RoleRequest request) {
        RoleResponse updatedRole = roleService.updateRole(id, request);
        return ResponseUtil.createSuccessResponse("Role updated successfully", updatedRole);
    }

    /**
     * Deletes a role by its ID.
     *
     * @param id The ID of the role to delete.
     * @return A ResponseEntity indicating the result of the deletion operation.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Integer id) {
        roleService.deleteRole(id);
        return ResponseUtil.createSuccessResponse("Role deleted successfully", null);
    }

    /**
     * Assigns a role to one or more users.
     *
     * @param id      The ID of the role to assign.
     * @param request The request containing a list of user IDs.
     * @return A response indicating the assignment was successful.
     */
    @PostMapping("/{id}/users")
    public ResponseEntity<ApiResponse<Void>> assignUsersToRole(
            @PathVariable Integer id,
            @Valid @RequestBody AssignUsersRequest request) {
        roleService.assignUsersToRole(id, request);
        return ResponseUtil.createSuccessResponse("Users assigned to role successfully", null);
    }

    /**
     * Unassigns a role from a specific user.
     *
     * @param id     The ID of the role to unassign.
     * @param userId The ID of the user to unassign the role from.
     * @return A response indicating the unassignment was successful.
     */
    @DeleteMapping("/{id}/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> unassignUserFromRole(
            @PathVariable("id") Integer id,
            @PathVariable("userId") Integer userId) {
        roleService.unassignUserFromRole(id, userId);
        return ResponseUtil.createSuccessResponse("User unassigned from role successfully", null);
    }
}
