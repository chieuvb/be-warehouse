package com.example.warehouse.repository;

import com.example.warehouse.entity.StockLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockLogRepository extends JpaRepository<StockLog, Long> {
    Page<StockLog> findByInventoryId(Long inventoryId, Pageable pageable);
}
