package com.example.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_inventories", uniqueConstraints = {
        // This key ensures one inventory record per product per zone in a warehouse.
        @UniqueConstraint(columnNames = {"product_id", "warehouse_id", "zone_id"}, name = "uk_product_location")
})
@ToString(exclude = "stockLogs")
@EqualsAndHashCode(exclude = "stockLogs")
public class ProductInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private WarehouseZone zone;

    @Builder.Default
    @Column(nullable = false)
    private Integer quantity = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Bidirectional relationship to its stock movement history
    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<StockLog> stockLogs;
}
