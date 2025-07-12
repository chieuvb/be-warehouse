package com.example.warehouse.repository;

import com.example.warehouse.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Integer> {
    // Find all top-level categories (those without a parent)
    List<ProductCategory> findByParentCategoryIsNull();

    // Check for name uniqueness within the same parent
    boolean existsByNameAndParentCategory(String name, ProductCategory parent);

    boolean existsByNameAndParentCategoryIsNull(String name);

    // Check for name uniqueness when updating (excluding the current category)
    boolean existsByNameAndParentCategoryAndIdNot(String name, ProductCategory parent, Integer id);

    boolean existsByNameAndParentCategoryIsNullAndIdNot(String name, Integer id);

    Optional<ProductCategory> findByName(String name);
}
