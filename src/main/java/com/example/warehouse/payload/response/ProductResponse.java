package com.example.warehouse.payload.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProductResponse {
    private Integer id;
    private String sku;
    private String barcode;
    private String name;
    private String description;
    private String categoryName;
    private String baseUnitName;
    private Integer minimumStock;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
