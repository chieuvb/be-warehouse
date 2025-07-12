package com.example.warehouse.mapper;

import com.example.warehouse.entity.AuditLog;
import com.example.warehouse.payload.response.AuditLogResponse;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {

    public AuditLogResponse toResponse(AuditLog auditLog) {
        if (auditLog == null) {
            return null;
        }

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
