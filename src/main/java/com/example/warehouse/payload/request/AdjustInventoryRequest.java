package com.example.warehouse.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdjustInventoryRequest {
    @NotNull(message = "Product ID is required")
    private Integer productId;

    @NotNull(message = "Warehouse ID is required")
    private Integer warehouseId;

    @NotNull(message = "Zone ID is required")
    private Integer zoneId;

    @NotNull(message = "Quantity change is required")
    private Integer quantityChange; // Can be positive (add) or negative (remove)

    @NotBlank(message = "A note is required for this adjustment")
    private String note;
}
