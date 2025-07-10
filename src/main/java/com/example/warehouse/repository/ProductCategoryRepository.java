package com.example.warehouse.repository;

import com.example.warehouse.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Integer> {

    // Custom query methods can be added here if needed
    // For example, to find a category by name:
    // Optional<ProductCategory> findByName(String name);

    // You can also add methods to handle specific queries related to ProductCategory
}
