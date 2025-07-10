package com.example.warehouse.mapper;

import com.example.warehouse.entity.Product;
import com.example.warehouse.payload.response.ProductResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toProductResponse(Product product) {
        if (product == null) {
            return null;
        }

        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .baseUnitName(product.getBaseUnit() != null ? product.getBaseUnit().getName() : null)
                .minimumStock(product.getMinimumStock())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
