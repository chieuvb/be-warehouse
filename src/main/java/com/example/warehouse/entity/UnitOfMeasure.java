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
@Table(name = "units_of_measure")
@ToString(exclude = "products")
@EqualsAndHashCode(exclude = "products")
public class UnitOfMeasure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 10)
    private String abbreviation;

    // The other side of the relationship: one unit can be used by many products
    @OneToMany(mappedBy = "baseUnit", fetch = FetchType.LAZY)
    private Set<Product> products;
}
