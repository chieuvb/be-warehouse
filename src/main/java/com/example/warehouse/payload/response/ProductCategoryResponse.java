package com.example.warehouse.payload.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY) // Hides the 'children' field if it's empty
public class ProductCategoryResponse {
    private Integer id;
    private String name;
    private String description;
    private Integer parentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ProductCategoryResponse> children;
}
