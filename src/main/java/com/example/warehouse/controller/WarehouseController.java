package com.example.warehouse.controller;

import com.example.warehouse.payload.request.WarehouseRequest;
import com.example.warehouse.payload.response.ApiResponse;
import com.example.warehouse.payload.response.WarehouseResponse;
import com.example.warehouse.service.WarehouseService;
import com.example.warehouse.utility.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/* * Controller for managing warehouses.
 * Provides endpoints to create, update, delete, and retrieve warehouses.
 */
@RestController
@RequestMapping("/warehouses")
@RequiredArgsConstructor
// Secure all endpoints to be accessible only by users with 'ROLE_ADMIN' or 'ROLE_WAREHOUSE_MANAGER'
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_WAREHOUSE_MANAGER')")
public class WarehouseController {

    private final WarehouseService warehouseService;

    /**
     * Retrieves all warehouses with pagination.
     *
     * @param pageable the pagination information
     * @return a paginated list of warehouses
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<WarehouseResponse>>> getAllWarehouses(
            @PageableDefault(size = 8, sort = "name") Pageable pageable) {
        Page<WarehouseResponse> warehouses = warehouseService.getAllWarehouses(pageable);
        return ResponseUtil.createSuccessResponse("Warehouses retrieved successfully", warehouses);
    }

    /**
     * Retrieves a warehouse by its ID.
     *
     * @param id the ID of the warehouse
     * @return the warehouse response
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WarehouseResponse>> getWarehouseById(@PathVariable Integer id) {
        WarehouseResponse warehouse = warehouseService.getWarehouseById(id);
        return ResponseUtil.createSuccessResponse("Warehouse retrieved successfully", warehouse);
    }

    /**
     * Creates a new warehouse.
     *
     * @param request the warehouse request containing details for the new warehouse
     * @return the created warehouse response
     */
    @PostMapping
    public ResponseEntity<ApiResponse<WarehouseResponse>> createWarehouse(@Valid @RequestBody WarehouseRequest request) {
        WarehouseResponse newWarehouse = warehouseService.createWarehouse(request);
        ApiResponse<WarehouseResponse> response = ApiResponse.success("Warehouse created successfully", newWarehouse);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Updates an existing warehouse.
     *
     * @param id      the ID of the warehouse to update
     * @param request the warehouse request containing updated details
     * @return the updated warehouse response
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WarehouseResponse>> updateWarehouse(
            @PathVariable Integer id,
            @Valid @RequestBody WarehouseRequest request) {
        WarehouseResponse updatedWarehouse = warehouseService.updateWarehouse(id, request);
        return ResponseUtil.createSuccessResponse("Warehouse updated successfully", updatedWarehouse);
    }

    /**
     * Deletes a warehouse by its ID.
     *
     * @param id the ID of the warehouse to delete
     * @return a success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWarehouse(@PathVariable Integer id) {
        warehouseService.deleteWarehouse(id);
        return ResponseUtil.createSuccessResponse("Warehouse deleted successfully", null);
    }
}
