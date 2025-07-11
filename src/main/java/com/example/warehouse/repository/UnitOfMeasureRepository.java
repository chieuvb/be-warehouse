package com.example.warehouse.repository;

import com.example.warehouse.entity.UnitOfMeasure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, Integer> {

    Optional<UnitOfMeasure> findByName(String name);

    Optional<UnitOfMeasure> findByAbbreviation(String abbreviation);

    boolean existsByName(String name);

    boolean existsByAbbreviation(String abbreviation);
}
