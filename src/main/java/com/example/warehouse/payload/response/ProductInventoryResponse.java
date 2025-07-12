package com.example.warehouse.payload.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProductInventoryResponse {
    private Long id;
    private String productName;
    private String productSku;
    private String warehouseName;
    private String zoneName;
    private Integer quantity;
    private LocalDateTime updatedAt;
}
