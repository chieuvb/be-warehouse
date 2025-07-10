package com.example.warehouse.repository;

import com.example.warehouse.entity.UnitOfMeasure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, Integer> {

    // Custom query methods can be added here if needed
    // For example, to find a unit by name:
    // Optional<UnitOfMeasure> findByName(String name);

    // You can also add methods to handle specific queries related to UnitOfMeasure
}
