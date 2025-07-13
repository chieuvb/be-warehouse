package com.example.warehouse.service;

import com.example.warehouse.entity.AuditLog;
import com.example.warehouse.entity.User;
import com.example.warehouse.enums.AuditActionEnum;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.mapper.AuditLogMapper;
import com.example.warehouse.payload.response.AuditLogResponse;
import com.example.warehouse.repository.AuditLogRepository;
import com.example.warehouse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;
    private final UserRepository userRepository;

    /**
     * Creates and saves an audit log entry. This is the primary method for logging actions.
     * It's designed to be called from other services (e.g., UserService, ProductService).
     *
     * @param actor         The user who performed the action. Can be null for system actions.
     * @param action        The type of action performed (e.g., CREATE_USER).
     * @param tableAffected The name of the database table that was affected.
     * @param objectId      The ID of the entity that was affected.
     * @param note          A descriptive note about the action.
     */
    @Transactional
    public void logAction(User actor, AuditActionEnum action, String tableAffected, String objectId, String note) {
        AuditLog auditLog = AuditLog.builder()
                .actor(actor)
                .action(action)
                .tableAffected(tableAffected)
                .objectId(objectId)
                .note(note)
                .build();
        auditLogRepository.save(auditLog);

        log.info("Audit log created: {}", auditLog.getAction());
    }

    /**
     * Retrieves a paginated list of all audit logs.
     *
     * @param pageable Pagination and sorting information.
     * @return A paginated list of audit log responses.
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAllAuditLogs(Pageable pageable) {
        log.info("Retrieving all audit logs");
        return auditLogRepository.findAll(pageable).map(auditLogMapper::toResponse);
    }

    /**
     * Retrieves a paginated list of audit logs for a specific user.
     *
     * @param username The user whose audit logs are to be retrieved.
     * @param pageable Pagination and sorting information.
     * @return A paginated list of audit log responses for the specified user.
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogsByActor(String username, Pageable pageable) {
        User actor = userRepository.findByUsername(username).orElseThrow(() ->
                new ResourceNotFoundException("User", "username", username));

        log.info("Retrieving audit logs for user: {}", username);
        return auditLogRepository.findByActor(actor, pageable).map(auditLogMapper::toResponse);
    }
}
