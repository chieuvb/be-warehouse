package com.example.warehouse.payload.response;

import com.example.warehouse.enums.AuditActionEnum;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogResponse {
    private Long id;
    private AuditActionEnum action;
    private String actorUsername;
    private String tableAffected;
    private String objectId;
    private String note;
    private LocalDateTime createdAt;
}
