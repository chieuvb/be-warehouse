package com.example.warehouse.repository;

import com.example.warehouse.entity.ProductInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductInventoryRepository extends JpaRepository<ProductInventory, Long> {
    Optional<ProductInventory> findByProductIdAndWarehouseIdAndZoneId(Integer productId, Integer warehouseId, Integer zoneId);
}
