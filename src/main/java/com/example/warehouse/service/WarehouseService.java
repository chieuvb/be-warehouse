package com.example.warehouse.service;

import com.example.warehouse.entity.Warehouse;
import com.example.warehouse.enums.AuditAction;
import com.example.warehouse.exception.ResourceConflictException;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.helper.GeneratorService;
import com.example.warehouse.mapper.WarehouseMapper;
import com.example.warehouse.payload.request.WarehouseRequest;
import com.example.warehouse.payload.response.WarehouseResponse;
import com.example.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing warehouses.
 * Provides methods to create, update, delete, and retrieve warehouses.
 */
@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;
    private final AuditLogService auditLogService;
    private final SecurityContextService securityContextService;
    private final GeneratorService generatorService;

    /**
     * Retrieves all warehouses with pagination.
     *
     * @param pageable the pagination information
     * @return a paginated list of warehouses
     */
    @Transactional(readOnly = true)
    public Page<WarehouseResponse> getAllWarehouses(Pageable pageable) {
        return warehouseRepository.findAll(pageable).map(warehouseMapper::toWarehouseResponse);
    }

    /**
     * Retrieves a warehouse by its ID.
     *
     * @param warehouseId the ID of the warehouse
     * @return the warehouse response
     * @throws ResourceNotFoundException if the warehouse is not found
     */
    @Transactional(readOnly = true)
    public WarehouseResponse getWarehouseById(Integer warehouseId) {
        return warehouseRepository.findById(warehouseId)
                .map(warehouseMapper::toWarehouseResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", warehouseId));
    }

    /**
     * Creates a new warehouse.
     *
     * @param request the warehouse request containing details for the new warehouse
     * @return the created warehouse response
     * @throws ResourceConflictException if a warehouse with the same name already exists
     */
    @Transactional
    public WarehouseResponse createWarehouse(WarehouseRequest request) {
        if (warehouseRepository.existsByName(request.getName())) {
            throw new ResourceConflictException("Warehouse", "name", request.getName());
        }

        String generatedCode = generatorService.generateWarehouseCode(request.getName());

        Warehouse warehouse = Warehouse.builder()
                .code(generatedCode)
                .name(request.getName())
                .address(request.getAddress())
                .isActive(request.getIsActive())
                .build();

        Warehouse savedWarehouse = warehouseRepository.save(warehouse);

        auditLogService.logAction(
                securityContextService.getCurrentActor(),
                AuditAction.CREATE_WAREHOUSE,
                "warehouses",
                savedWarehouse.getId().toString(),
                String.format("Created warehouse '%s'", savedWarehouse.getName())
        );

        return warehouseMapper.toWarehouseResponse(savedWarehouse);
    }

    /**
     * Updates an existing warehouse.
     *
     * @param warehouseId the ID of the warehouse to update
     * @param request     the warehouse request containing updated details
     * @return the updated warehouse response
     * @throws ResourceNotFoundException if the warehouse is not found
     * @throws ResourceConflictException  if a warehouse with the same name already exists
     */
    @Transactional
    public WarehouseResponse updateWarehouse(Integer warehouseId, WarehouseRequest request) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", warehouseId));

        // Check if the new name conflicts with another existing warehouse
        warehouseRepository.findByName(request.getName()).ifPresent(existingWarehouse -> {
            if (!existingWarehouse.getId().equals(warehouseId)) {
                throw new ResourceConflictException("Warehouse", "name", request.getName());
            }
        });

        warehouse.setName(request.getName());
        warehouse.setAddress(request.getAddress());
        warehouse.setIsActive(request.getIsActive());

        Warehouse updatedWarehouse = warehouseRepository.save(warehouse);

        auditLogService.logAction(
                securityContextService.getCurrentActor(),
                AuditAction.UPDATE_WAREHOUSE,
                "warehouses",
                updatedWarehouse.getId().toString(),
                String.format("Updated warehouse '%s'", updatedWarehouse.getName())
        );

        return warehouseMapper.toWarehouseResponse(updatedWarehouse);
    }

    /**
     * Deletes a warehouse by its ID.
     *
     * @param warehouseId the ID of the warehouse to delete
     * @throws ResourceNotFoundException if the warehouse is not found
     */
    @Transactional
    public void deleteWarehouse(Integer warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", warehouseId));

        // Note: In a real-world scenario, you would first check if this warehouse has any inventory.
        // If it does, you should prevent deletion and return a 409-Conflict error.
        // For this example, we perform a direct deletion.

        auditLogService.logAction(
                securityContextService.getCurrentActor(),
                AuditAction.DELETE_WAREHOUSE,
                "warehouses",
                warehouseId.toString(),
                String.format("Deleted warehouse '%s'", warehouse.getName())
        );

        warehouseRepository.delete(warehouse);
    }
}
