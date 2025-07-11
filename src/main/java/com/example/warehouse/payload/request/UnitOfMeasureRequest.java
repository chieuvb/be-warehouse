package com.example.warehouse.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UnitOfMeasureRequest {

    @NotBlank(message = "Unit name is required")
    @Size(max = 50, message = "Unit name cannot exceed 50 characters")
    private String name;

    @NotBlank(message = "Abbreviation is required")
    @Size(max = 10, message = "Abbreviation cannot exceed 10 characters")
    private String abbreviation;
}
