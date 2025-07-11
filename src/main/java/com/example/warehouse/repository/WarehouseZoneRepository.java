package com.example.warehouse.repository;

import com.example.warehouse.entity.Warehouse;
import com.example.warehouse.entity.WarehouseZone;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseZoneRepository extends JpaRepository<WarehouseZone, Integer> {

    List<WarehouseZone> findByWarehouseId(Integer warehouseId);

    Optional<WarehouseZone> findByWarehouseIdAndId(Integer warehouseId, Integer zoneId);

    boolean existsByWarehouseAndName(Warehouse warehouse, String name);

    boolean existsByCode(String code);

    boolean existsByWarehouseAndNameAndIdNot(Warehouse warehouse, @NotBlank(message = "Zone name is required") @Size(max = 100, message = "Zone name cannot exceed 100 characters") String name, Integer zoneId);
}
