package com.example.warehouse.payload.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MoveInventoryRequest {

    @NotNull(message = "Product ID is required")
    private Integer productId;

    @NotNull(message = "Warehouse ID is required")
    private Integer warehouseId;

    @NotNull(message = "Source Zone ID is required")
    private Integer sourceZoneId;

    @NotNull(message = "Destination Zone ID is required")
    private Integer destinationZoneId;

    @NotNull(message = "Quantity to move is required")
    @Min(value = 1, message = "Quantity to move must be at least 1")
    private Integer quantity;

    @NotBlank(message = "A note is required for this movement")
    private String note;
}
