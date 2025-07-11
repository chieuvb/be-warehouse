package com.example.warehouse.controller;

import com.example.warehouse.payload.request.WarehouseRequest;
import com.example.warehouse.payload.request.WarehouseZoneRequest;
import com.example.warehouse.payload.response.ApiResponse;
import com.example.warehouse.payload.response.WarehouseResponse;
import com.example.warehouse.payload.response.WarehouseZoneResponse;
import com.example.warehouse.service.WarehouseService;
import com.example.warehouse.service.WarehouseZoneService;
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

import java.util.List;

/**
 * Controller for managing warehouses and their zones.
 * Provides endpoints to create, update, delete, and retrieve warehouses and zones.
 */
@RestController
@RequestMapping("/warehouses")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final WarehouseZoneService zoneService;

//    --- Warehouse Zone Service ---

    /**
     * Retrieves all warehouses with pagination.
     *
     * @param pageable the pagination information
     * @return a paginated list of warehouses
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<WarehouseResponse>>> getAllWarehouses(
            @PageableDefault(sort = "name") Pageable pageable) {
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

//    --- Warehouse Zone Endpoints ---

    /**
     * Creates a new zone within a specific warehouse.
     *
     * @param warehouseId The ID of the parent warehouse.
     * @param request     The request body containing the zone's name.
     * @return The created warehouse zone response.
     */
    @PostMapping("/{warehouseId}/zones")
    public ResponseEntity<ApiResponse<WarehouseZoneResponse>> createZone(
            @PathVariable Integer warehouseId,
            @Valid @RequestBody WarehouseZoneRequest request) {
        WarehouseZoneResponse newZone = zoneService.createZone(warehouseId, request);
        ApiResponse<WarehouseZoneResponse> response = ApiResponse.success("Zone created successfully", newZone);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves all zones for a specific warehouse.
     *
     * @param warehouseId The ID of the parent warehouse.
     * @return A list of warehouse zone responses.
     */
    @GetMapping("/{warehouseId}/zones")
    public ResponseEntity<ApiResponse<List<WarehouseZoneResponse>>> getZonesForWarehouse(@PathVariable Integer warehouseId) {
        List<WarehouseZoneResponse> zones = zoneService.getZonesForWarehouse(warehouseId);
        return ResponseUtil.createSuccessResponse("Zones retrieved successfully", zones);
    }

    /**
     * Retrieves a specific zone by its ID within a warehouse.
     *
     * @param warehouseId The ID of the parent warehouse.
     * @param zoneId      The ID of the zone to retrieve.
     * @return The warehouse zone response.
     */
    @GetMapping("/{warehouseId}/zones/{zoneId}")
    public ResponseEntity<ApiResponse<WarehouseZoneResponse>> getZoneById(
            @PathVariable Integer warehouseId,
            @PathVariable Integer zoneId) {
        WarehouseZoneResponse zone = zoneService.getZoneById(warehouseId, zoneId);
        return ResponseUtil.createSuccessResponse("Zone retrieved successfully", zone);
    }

    /**
     * Retrieves a specific zone by its ID within a warehouse.
     *
     * @param warehouseId The ID of the parent warehouse.
     * @param zoneId      The ID of the zone to retrieve.
     * @return The warehouse zone response.
     */
    @PutMapping("/{warehouseId}/zones/{zoneId}")
    public ResponseEntity<ApiResponse<WarehouseZoneResponse>> updateZone(
            @PathVariable Integer warehouseId,
            @PathVariable Integer zoneId,
            @Valid @RequestBody WarehouseZoneRequest request) {
        WarehouseZoneResponse updatedZone = zoneService.updateZone(warehouseId, zoneId, request);
        return ResponseUtil.createSuccessResponse("Zone updated successfully", updatedZone);
    }

    /**
     * Deletes a zone from a specific warehouse.
     *
     * @param warehouseId The ID of the parent warehouse.
     * @param zoneId      The ID of the zone to delete.
     * @return A success response.
     */
    @DeleteMapping("/{warehouseId}/zones/{zoneId}")
    public ResponseEntity<ApiResponse<Void>> deleteZone(
            @PathVariable Integer warehouseId,
            @PathVariable Integer zoneId) {
        zoneService.deleteZone(warehouseId, zoneId);
        return ResponseUtil.createSuccessResponse("Zone deleted successfully", null);
    }
}
