package com.example.warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "stock_logs")
public class StockLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "inventory_id")
    private ProductInventory inventory;

    @ManyToOne
    @JoinColumn(name = "actor_id")
    private User actor;

    @Enumerated(EnumType.STRING)
    private StockLogType type;

    @Column(name = "quantity_before")
    private Integer quantityBefore;

    @Column(name = "quantity_change")
    private Integer quantityChange;

    @Column(name = "quantity_after")
    private Integer quantityAfter;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "reference_id")
    private Integer referenceId;

    private String note;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum StockLogType {
        INITIAL_STOCK, GOODS_RECEIPT, GOODS_ISSUE,
        ADJUSTMENT_IN, ADJUSTMENT_OUT, STOCK_COUNT
    }
}
