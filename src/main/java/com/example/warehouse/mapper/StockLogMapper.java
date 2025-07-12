package com.example.warehouse.mapper;

import com.example.warehouse.entity.StockLog;
import com.example.warehouse.payload.response.StockLogResponse;
import org.springframework.stereotype.Component;

@Component
public class StockLogMapper {

    public StockLogResponse toResponse(StockLog log) {
        if (log == null) {
            return null;
        }

        return StockLogResponse.builder()
                .id(log.getId())
                .transactionType(log.getType())
                .productSku(log.getInventory().getProduct().getSku())
                .warehouseName(log.getInventory().getWarehouse().getName())
                .zoneName(log.getInventory().getZone().getName())
                .quantityBefore(log.getQuantityBefore())
                .quantityChange(log.getQuantityChange())
                .quantityAfter(log.getQuantityAfter())
                .actorUsername(log.getActor() != null ? log.getActor().getUsername() : "SYSTEM")
                .note(log.getNote())
                .createdAt(log.getCreatedAt())
                .build();
    }
}

