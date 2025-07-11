package com.example.warehouse.payload.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UnitOfMeasureResponse {
    private Integer id;
    private String name;
    private String abbreviation;
}
