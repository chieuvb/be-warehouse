package com.example.warehouse.mapper;

import com.example.warehouse.entity.WarehouseZone;
import com.example.warehouse.payload.response.WarehouseZoneResponse;
import org.springframework.stereotype.Component;

@Component
public class WarehouseZoneMapper {

    public WarehouseZoneResponse toWarehouseZoneResponse(WarehouseZone zone) {
        if (zone == null) {
            return null;
        }

        return WarehouseZoneResponse.builder()
                .id(zone.getId())
                .name(zone.getName())
                .code(zone.getCode())
                .warehouseId(zone.getWarehouse() != null ? zone.getWarehouse().getId() : null)
                .warehouseName(zone.getWarehouse() != null ? zone.getWarehouse().getName() : null)
                .build();
    }
}
