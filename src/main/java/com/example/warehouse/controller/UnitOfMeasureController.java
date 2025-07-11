package com.example.warehouse.controller;

import com.example.warehouse.payload.request.UnitOfMeasureRequest;
import com.example.warehouse.payload.response.ApiResponse;
import com.example.warehouse.payload.response.UnitOfMeasureResponse;
import com.example.warehouse.service.UnitOfMeasureService;
import com.example.warehouse.utility.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing units of measure in the warehouse management system.
 * Provides endpoints to create, retrieve, update, and delete units of measure.
 */
@RestController
@RequestMapping("/units-of-measure")
@RequiredArgsConstructor
// Secure all endpoints to be accessible only by users with 'ROLE_ADMIN' or 'ROLE_MANAGER'
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class UnitOfMeasureController {

    private final UnitOfMeasureService unitService;

    /**
     * Retrieves all units of measure.
     *
     * @return a list of UnitOfMeasureResponse objects representing all units of measure
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UnitOfMeasureResponse>>> getAllUnits() {
        List<UnitOfMeasureResponse> units = unitService.getAllUnits();
        return ResponseUtil.createSuccessResponse("Units of measure retrieved successfully", units);
    }

    /**
     * Retrieves a unit of measure by its ID.
     *
     * @param id the ID of the unit of measure
     * @return a UnitOfMeasureResponse object representing the unit of measure
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UnitOfMeasureResponse>> getUnitById(@PathVariable Integer id) {
        UnitOfMeasureResponse unit = unitService.getUnitById(id);
        return ResponseUtil.createSuccessResponse("Unit of measure retrieved successfully", unit);
    }

    /**
     * Creates a new unit of measure.
     *
     * @param request the request object containing details for the new unit of measure
     * @return a UnitOfMeasureResponse object representing the created unit of measure
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UnitOfMeasureResponse>> createUnit(@Valid @RequestBody UnitOfMeasureRequest request) {
        UnitOfMeasureResponse newUnit = unitService.createUnit(request);
        ApiResponse<UnitOfMeasureResponse> response = ApiResponse.success("Unit of measure created successfully", newUnit);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Updates an existing unit of measure.
     *
     * @param id      the ID of the unit of measure to update
     * @param request the request object containing updated details for the unit of measure
     * @return a UnitOfMeasureResponse object representing the updated unit of measure
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UnitOfMeasureResponse>> updateUnit(
            @PathVariable Integer id,
            @Valid @RequestBody UnitOfMeasureRequest request) {
        UnitOfMeasureResponse updatedUnit = unitService.updateUnit(id, request);
        return ResponseUtil.createSuccessResponse("Unit of measure updated successfully", updatedUnit);
    }

    /**
     * Deletes a unit of measure by its ID.
     *
     * @param id the ID of the unit of measure to delete
     * @return a response indicating the deletion was successful
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUnit(@PathVariable Integer id) {
        unitService.deleteUnit(id);
        return ResponseUtil.createSuccessResponse("Unit of measure deleted successfully", null);
    }
}
