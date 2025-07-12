package com.example.warehouse.mapper;

import com.example.warehouse.entity.ProductCategory;
import com.example.warehouse.payload.response.ProductCategoryResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductCategoryMapper {

    public ProductCategoryResponse toResponse(ProductCategory category) {
        if (category == null) {
            return null;
        }

        // Recursively map children
        List<ProductCategoryResponse> children = (category.getChildCategories() == null)
                ? Collections.emptyList()
                : category.getChildCategories().stream()
                .map(this::toResponse) // The recursive call
                .collect(Collectors.toList());

        return ProductCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParentCategory() != null ? category.getParentCategory().getId() : null)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .children(children)
                .build();
    }
}
