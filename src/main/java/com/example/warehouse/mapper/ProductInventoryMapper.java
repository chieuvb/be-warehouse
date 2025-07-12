package com.example.warehouse.mapper;

import com.example.warehouse.entity.ProductInventory;
import com.example.warehouse.payload.response.ProductInventoryResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductInventoryMapper {

    public ProductInventoryResponse toResponse(ProductInventory inventory) {
        if (inventory == null) {
            return null;
        }

        return ProductInventoryResponse.builder()
                .id(inventory.getId())
                .productName(inventory.getProduct().getName())
                .productSku(inventory.getProduct().getSku())
                .warehouseName(inventory.getWarehouse().getName())
                .zoneName(inventory.getZone().getName())
                .quantity(inventory.getQuantity())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}
