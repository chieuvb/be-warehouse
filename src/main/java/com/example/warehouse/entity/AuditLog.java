package com.example.warehouse.entity;

import com.example.warehouse.enums.AuditAction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Using Long for log tables

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Column(name = "table_affected", nullable = false, length = 50)
    private String tableAffected;

    @Column(name = "object_id", nullable = false, length = 50)
    private String objectId;

    @Column(columnDefinition = "TEXT")
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
