package com.example.warehouse.payload.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Represents a request payload for creating or updating a product in the warehouse management system.
 * This class includes validation annotations to ensure that required fields are provided and valid.
 */
@Data
public class ProductRequest {
    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Category ID is required")
    private Integer categoryId;

    @NotNull(message = "Base Unit ID is required")
    private Integer baseUnitId;

    @NotNull(message = "Minimum stock is required")
    @Min(value = 0, message = "Minimum stock cannot be negative")
    private Integer minimumStock;

    @NotNull(message = "Active status is required")
    private Boolean isActive;
}
