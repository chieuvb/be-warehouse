package com.example.warehouse.mapper;

import com.example.warehouse.entity.UnitOfMeasure;
import com.example.warehouse.payload.response.UnitOfMeasureResponse;
import org.springframework.stereotype.Component;

@Component
public class UnitOfMeasureMapper {

    public UnitOfMeasureResponse toUnitOfMeasureResponse(UnitOfMeasure unit) {
        if (unit == null) {
            return null;
        }

        return UnitOfMeasureResponse.builder()
                .id(unit.getId())
                .name(unit.getName())
                .abbreviation(unit.getAbbreviation())
                .build();
    }
}
