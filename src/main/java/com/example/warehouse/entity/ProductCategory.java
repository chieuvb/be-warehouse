package com.example.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_categories")
@ToString(exclude = {"parent", "children", "products"})
@EqualsAndHashCode(exclude = {"parent", "children", "products"})
public class ProductCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Self-referencing relationship for parent
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ProductCategory parent;

    // Self-referencing relationship for children
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private Set<ProductCategory> children;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Bidirectional relationship to Products
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private Set<Product> products;
}
