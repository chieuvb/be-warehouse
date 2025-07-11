package com.example.warehouse.controller;

import com.example.warehouse.payload.response.ApiResponse;
import com.example.warehouse.payload.response.AuditLogResponse;
import com.example.warehouse.service.AuditLogService;
import com.example.warehouse.utility.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing audit logs in the warehouse management system.
 * Provides endpoints to retrieve audit logs with pagination.
 */
@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * Retrieves a paginated list of all audit logs.
     *
     * @param pageable Pagination information.
     * @return A ResponseEntity containing a paginated list of AuditLogResponse DTOs.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAllAuditLogs(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AuditLogResponse> auditLogs = auditLogService.getAllAuditLogs(pageable);
        return ResponseUtil.createSuccessResponse("Audit logs retrieved successfully", auditLogs);
    }

    /**
     * Retrieves a paginated list of audit logs filtered by the actor.
     *
     * @param pageable Pagination information.
     * @return A ResponseEntity containing a paginated list of AuditLogResponse DTOs for the specified actor.
     */
    @GetMapping("/{actor}")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLogsByActor(
            @PathVariable String actor,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AuditLogResponse> auditLogs = auditLogService.getAuditLogsByActor(actor, pageable);
        return ResponseUtil.createSuccessResponse("Audit logs for actor retrieved successfully", auditLogs);
    }
}
