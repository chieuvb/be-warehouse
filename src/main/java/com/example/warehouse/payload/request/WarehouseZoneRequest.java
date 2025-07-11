package com.example.warehouse.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WarehouseZoneRequest {

    @NotBlank(message = "Zone name is required")
    @Size(max = 100, message = "Zone name cannot exceed 100 characters")
    private String name;

}
