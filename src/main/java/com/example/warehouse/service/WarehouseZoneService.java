package com.example.warehouse.service;

import com.example.warehouse.entity.Warehouse;
import com.example.warehouse.entity.WarehouseZone;
import com.example.warehouse.enums.AuditActionEnum;
import com.example.warehouse.exception.ResourceConflictException;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.helper.GeneratorService;
import com.example.warehouse.mapper.WarehouseZoneMapper;
import com.example.warehouse.payload.request.WarehouseZoneRequest;
import com.example.warehouse.payload.response.WarehouseZoneResponse;
import com.example.warehouse.repository.WarehouseRepository;
import com.example.warehouse.repository.WarehouseZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing warehouse zones.
 * Provides methods to create, retrieve, and delete zones within a warehouse.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseZoneService {

    private final WarehouseZoneRepository zoneRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseZoneMapper zoneMapper;
    private final GeneratorService generatorService;
    private final AuditLogService auditLogService;
    private final SecurityContextService securityContextService;

    /**
     * Creates a new zone in the specified warehouse.
     *
     * @param warehouseId the ID of the warehouse
     * @param request     the request containing zone details
     * @return the created WarehouseZoneResponse
     * @throws ResourceNotFoundException if the warehouse does not exist
     * @throws ResourceConflictException if a zone with the same name already exists in the warehouse
     */
    @Transactional
    public WarehouseZoneResponse createZone(Integer warehouseId, WarehouseZoneRequest request) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", warehouseId));

        if (zoneRepository.existsByWarehouseAndName(warehouse, request.getName())) {
            throw new ResourceConflictException("A zone with the name '" + request.getName() + "' already exists in this warehouse.");
        }

        String generatedCode = generatorService.generateWarehouseZoneCode(warehouse, request.getName());

        WarehouseZone zone = WarehouseZone.builder()
                .name(request.getName())
                .code(generatedCode)
                .warehouse(warehouse)
                .build();

        WarehouseZone savedZone = zoneRepository.save(zone);

        auditLogService.logAction(
                securityContextService.getCurrentActor(),
                AuditActionEnum.CREATE_ZONE,
                "warehouse_zones",
                savedZone.getId().toString(),
                String.format("Created zone '%s' in warehouse '%s'", savedZone.getName(), warehouse.getName())
        );

        log.info("Created new zone: {} with code: {} in warehouse: {}", savedZone.getName(), savedZone.getCode(), warehouse.getName());
        return zoneMapper.toWarehouseZoneResponse(savedZone);
    }

    /**
     * Retrieves all zones for a specific warehouse.
     *
     * @param warehouseId the ID of the warehouse
     * @return a list of WarehouseZoneResponse objects
     * @throws ResourceNotFoundException if the warehouse does not exist
     */
    @Transactional(readOnly = true)
    public List<WarehouseZoneResponse> getZonesForWarehouse(Integer warehouseId) {
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new ResourceNotFoundException("Warehouse", "id", warehouseId);
        }

        log.info("Retrieving all zones for warehouse with ID: {}", warehouseId);
        return zoneRepository.findByWarehouseId(warehouseId).stream()
                .map(zoneMapper::toWarehouseZoneResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific zone by its ID within a warehouse.
     *
     * @param warehouseId the ID of the warehouse
     * @param zoneId      the ID of the zone
     * @return the WarehouseZoneResponse for the specified zone
     * @throws ResourceNotFoundException if the warehouse or zone does not exist
     */
    @Transactional(readOnly = true)
    public WarehouseZoneResponse getZoneById(Integer warehouseId, Integer zoneId) {
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new ResourceNotFoundException("Warehouse", "id", warehouseId);
        }

        WarehouseZone zone = zoneRepository.findByWarehouseIdAndId(warehouseId, zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("WarehouseZone", "id", zoneId + " not found in warehouse " + warehouseId));

        log.info("Retrieving zone with ID: {} in warehouse with ID: {}", zoneId, warehouseId);
        return zoneMapper.toWarehouseZoneResponse(zone);
    }

    /**
     * Updates an existing zone in the specified warehouse.
     *
     * @param warehouseId the ID of the warehouse
     * @param zoneId      the ID of the zone to update
     * @param request     the request containing updated zone details
     * @return the updated WarehouseZoneResponse
     * @throws ResourceNotFoundException if the warehouse or zone does not exist
     * @throws ResourceConflictException if a zone with the same name already exists in the warehouse
     */
    @Transactional(readOnly = true)
    public WarehouseZoneResponse updateZone(Integer warehouseId, Integer zoneId, WarehouseZoneRequest request) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", warehouseId));

        WarehouseZone zone = zoneRepository.findByWarehouseIdAndId(warehouseId, zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("WarehouseZone", "id", zoneId + " not found in warehouse " + warehouseId));

        if (zoneRepository.existsByWarehouseAndNameAndIdNot(warehouse, request.getName(), zoneId)) {
            throw new ResourceConflictException("A zone with the name '" + request.getName() + "' already exists in this warehouse.");
        }

        zone.setName(request.getName());
        zone.setCode(generatorService.generateWarehouseZoneCode(warehouse, request.getName()));

        WarehouseZone updatedZone = zoneRepository.save(zone);

        auditLogService.logAction(
                securityContextService.getCurrentActor(),
                AuditActionEnum.UPDATE_ZONE,
                "warehouse_zones",
                updatedZone.getId().toString(),
                String.format("Updated zone '%s' in warehouse '%s'", updatedZone.getName(), warehouse.getName())
        );

        log.info("Updated zone: {} with code: {} in warehouse: {}", updatedZone.getName(), updatedZone.getCode(), warehouse.getName());
        return zoneMapper.toWarehouseZoneResponse(updatedZone);
    }

    /**
     * Deletes a zone from the specified warehouse.
     *
     * @param warehouseId the ID of the warehouse
     * @param zoneId      the ID of the zone to delete
     * @throws ResourceNotFoundException if the zone does not exist in the warehouse
     * @throws ResourceConflictException if the zone contains inventory items
     */
    @Transactional
    public void deleteZone(Integer warehouseId, Integer zoneId) {
        WarehouseZone zone = zoneRepository.findByWarehouseIdAndId(warehouseId, zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("WarehouseZone", "id", zoneId + " not found in warehouse " + warehouseId));

        // In a real application, check for dependencies (e.g., inventory) before deleting.
        if (!zone.getProductInventories().isEmpty()) {
            throw new ResourceConflictException("Cannot delete zone: It contains inventory items.");
        }

        auditLogService.logAction(
                securityContextService.getCurrentActor(),
                AuditActionEnum.DELETE_ZONE,
                "warehouse_zones",
                zoneId.toString(),
                String.format("Deleted zone '%s' from warehouse '%s'", zone.getName(), zone.getWarehouse().getName())
        );

        log.info("Deleting zone: {} with code: {} from warehouse: {}", zone.getName(), zone.getCode(), zone.getWarehouse().getName());
        zoneRepository.delete(zone);
    }
}
