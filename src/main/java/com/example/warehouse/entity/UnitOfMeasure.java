package com.example.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a Unit of Measure for products.
 * Corresponds to the `units_of_measure` table.
 */
@Entity
@Table(name = "units_of_measure")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitOfMeasure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, length = 50, unique = true)
    private String name;

    @Column(name = "abbreviation", nullable = false, length = 10, unique = true)
    private String abbreviation;

    /**
     * A set of all products that use this unit of measure as their base unit.
     * This is the inverse side of the relationship defined in the Product entity.
     * It's primarily used for validation, such as preventing the deletion of a
     * unit of measure that is currently in use.
     */
    @OneToMany(mappedBy = "baseUnit", fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private Set<Product> products = new HashSet<>();

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        UnitOfMeasure that = (UnitOfMeasure) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
