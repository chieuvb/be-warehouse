package com.example.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "warehouse_zones", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"warehouse_id", "code"}, name = "uk_zone_code")
})
@ToString(exclude = "inventories")
@EqualsAndHashCode(exclude = "inventories")
public class WarehouseZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    // Bidirectional relationship to inventories located in this zone
    @OneToMany(mappedBy = "zone", fetch = FetchType.LAZY)
    private Set<ProductInventory> inventories;
}
