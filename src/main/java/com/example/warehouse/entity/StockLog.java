package com.example.warehouse.entity;

import com.example.warehouse.enums.StockLogType;
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
@Table(name = "stock_logs")
public class StockLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Using Long for log tables is a good practice for scalability

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_id", nullable = false)
    private ProductInventory inventory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockLogType type;

    @Column(name = "quantity_before", nullable = false)
    private Integer quantityBefore;

    @Column(name = "quantity_change", nullable = false)
    private Integer quantityChange;

    @Column(name = "quantity_after", nullable = false)
    private Integer quantityAfter;

    @Column(name = "reference_type", length = 50)
    private String referenceType; // e.g., "PURCHASE_ORDER", "SALES_ORDER"

    @Column(name = "reference_id")
    private Integer referenceId; // e.g., The ID of the purchase or sales order

    @Column(columnDefinition = "TEXT")
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
