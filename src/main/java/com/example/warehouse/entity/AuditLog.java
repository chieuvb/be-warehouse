package com.example.warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private AuditAction action;

    @ManyToOne
    @JoinColumn(name = "actor_id")
    private User actor;

    @Column(name = "table_affected", nullable = false, length = 50)
    private String tableAffected;

    @Column(name = "object_id", nullable = false, length = 50)
    private String objectId;

    private String note;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum AuditAction {
        CREATE_INVENTORY, UPDATE_INVENTORY, DELETE_INVENTORY
    }
}
