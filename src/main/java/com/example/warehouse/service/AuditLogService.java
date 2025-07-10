package com.example.warehouse.service;

import com.example.warehouse.entity.AuditLog;
import com.example.warehouse.entity.User;
import com.example.warehouse.enums.AuditAction;
import com.example.warehouse.payload.response.AuditLogResponse;
import com.example.warehouse.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Creates and saves an audit log entry. This is the core method for logging user actions.
     *
     * @param actor         The user who performed the action. Can be null for system actions.
     * @param action        The type of action performed (e.g., CREATE, UPDATE, DELETE).
     * @param tableAffected The name of the database table that was affected.
     * @param objectId      The ID of the entity that was affected.
     * @param note          An optional, human-readable note describing the change.
     */
    @Transactional
    public void logAction(User actor, AuditAction action, String tableAffected, String objectId, String note) {
        AuditLog auditLog = AuditLog.builder()
                .actor(actor)
                .action(action)
                .tableAffected(tableAffected)
                .objectId(objectId)
                .note(note)
                .build();

        auditLogRepository.save(auditLog);
    }

    /**
     * Retrieves a paginated list of all audit logs.
     *
     * @param pageable Pagination information.
     * @return A page of AuditLogResponse DTOs.
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable)
                .map(this::mapToAuditLogResponse);
    }

    /**
     * Maps an AuditLog entity to an AuditLogResponse DTO.
     *
     * @param auditLog The entity to map.
     * @return The mapped DTO.
     */
    private AuditLogResponse mapToAuditLogResponse(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .action(auditLog.getAction())
                .actorUsername(auditLog.getActor() != null ? auditLog.getActor().getUsername() : "SYSTEM")
                .tableAffected(auditLog.getTableAffected())
                .objectId(auditLog.getObjectId())
                .note(auditLog.getNote())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}
