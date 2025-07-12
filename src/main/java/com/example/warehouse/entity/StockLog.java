package com.example.warehouse.entity;

import com.example.warehouse.enums.ReferenceActionEnum;
import com.example.warehouse.enums.StockLogEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Records changes in product stock.
 * Corresponds to the `stock_logs` table.
 */
@Entity
@Table(name = "stock_logs")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false, referencedColumnName = "id")
    @ToString.Exclude
    private ProductInventory inventory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", referencedColumnName = "id")
    @ToString.Exclude
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private StockLogEnum type;

    @Column(name = "quantity_before", nullable = false)
    private Integer quantityBefore;

    @Column(name = "quantity_change", nullable = false)
    private Integer quantityChange;

    @Column(name = "quantity_after", nullable = false)
    private Integer quantityAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", length = 50)
    private ReferenceActionEnum referenceType;

    @Column(name = "reference_id", length = 50)
    private String referenceId;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        StockLog stockLog = (StockLog) o;
        return getId() != null && Objects.equals(getId(), stockLog.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
