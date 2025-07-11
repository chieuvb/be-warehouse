package com.example.warehouse.mapper;

import com.example.warehouse.entity.Warehouse;
import com.example.warehouse.payload.response.WarehouseResponse;
import org.springframework.stereotype.Component;

@Component
public class WarehouseMapper {

    public WarehouseResponse toWarehouseResponse(Warehouse warehouse) {
        if (warehouse == null) {
            return null;
        }

        return WarehouseResponse.builder()
                .id(warehouse.getId())
                .name(warehouse.getName())
                .code(warehouse.getCode())
                .address(warehouse.getAddress())
                .isActive(warehouse.getIsActive())
                .createdAt(warehouse.getCreatedAt())
                .updatedAt(warehouse.getUpdatedAt())
                .build();
    }
}
