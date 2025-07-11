package com.example.warehouse.payload.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WarehouseZoneResponse {
    private Integer id;
    private String name;
    private String code;
    private Integer warehouseId;
    private String warehouseName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
