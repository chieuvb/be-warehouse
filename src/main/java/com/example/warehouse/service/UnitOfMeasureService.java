package com.example.warehouse.service;

import com.example.warehouse.entity.UnitOfMeasure;
import com.example.warehouse.enums.AuditActionEnum;
import com.example.warehouse.exception.ResourceConflictException;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.mapper.UnitOfMeasureMapper;
import com.example.warehouse.payload.request.UnitOfMeasureRequest;
import com.example.warehouse.payload.response.UnitOfMeasureResponse;
import com.example.warehouse.repository.UnitOfMeasureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing units of measure in the warehouse management system.
 * This service handles CRUD operations, including creation, retrieval, updating, and deletion of units of measure.
 */
@Service
@RequiredArgsConstructor
public class UnitOfMeasureService {

    private final UnitOfMeasureRepository unitRepository;
    private final UnitOfMeasureMapper unitMapper;
    private final AuditLogService auditLogService;
    private final SecurityContextService securityContextService;

    /**
     * Retrieves all units of measure.
     *
     * @return a list of UnitOfMeasureResponse objects representing all units of measure
     */
    @Transactional(readOnly = true)
    public List<UnitOfMeasureResponse> getAllUnits() {
        return unitRepository.findAll().stream()
                .map(unitMapper::toUnitOfMeasureResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a unit of measure by its ID.
     *
     * @param unitId the ID of the unit of measure
     * @return a UnitOfMeasureResponse object representing the unit of measure
     * @throws ResourceNotFoundException if the unit of measure is not found
     */
    @Transactional(readOnly = true)
    public UnitOfMeasureResponse getUnitById(Integer unitId) {
        return unitRepository.findById(unitId)
                .map(unitMapper::toUnitOfMeasureResponse)
                .orElseThrow(() -> new ResourceNotFoundException("UnitOfMeasure", "id", unitId));
    }

    /**
     * Creates a new unit of measure.
     *
     * @param request the request object containing details for the new unit of measure
     * @return a UnitOfMeasureResponse object representing the created unit of measure
     * @throws ResourceConflictException if a unit with the same name or abbreviation already exists
     */
    @Transactional
    public UnitOfMeasureResponse createUnit(UnitOfMeasureRequest request) {
        if (unitRepository.existsByName(request.getName())) {
            throw new ResourceConflictException("UnitOfMeasure", "name", request.getName());
        }
        if (unitRepository.existsByAbbreviation(request.getAbbreviation())) {
            throw new ResourceConflictException("UnitOfMeasure", "abbreviation", request.getAbbreviation());
        }

        UnitOfMeasure unit = UnitOfMeasure.builder()
                .name(request.getName())
                .abbreviation(request.getAbbreviation())
                .build();

        UnitOfMeasure savedUnit = unitRepository.save(unit);

        auditLogService.logAction(
                securityContextService.getCurrentActor(),
                AuditActionEnum.CREATE_UNIT_OF_MEASURE,
                "units_of_measure",
                savedUnit.getId().toString(),
                String.format("Created unit of measure '%s' (%s)", savedUnit.getName(), savedUnit.getAbbreviation())
        );

        return unitMapper.toUnitOfMeasureResponse(savedUnit);
    }

    /**
     * Updates an existing unit of measure.
     *
     * @param unitId  the ID of the unit of measure to update
     * @param request the request object containing updated details for the unit of measure
     * @return a UnitOfMeasureResponse object representing the updated unit of measure
     * @throws ResourceNotFoundException if the unit of measure is not found
     * @throws ResourceConflictException if a unit with the same name or abbreviation already exists
     */
    @Transactional
    public UnitOfMeasureResponse updateUnit(Integer unitId, UnitOfMeasureRequest request) {
        UnitOfMeasure unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResourceNotFoundException("UnitOfMeasure", "id", unitId));

        // Check for name conflict
        unitRepository.findByName(request.getName()).ifPresent(existingUnit -> {
            if (!existingUnit.getId().equals(unitId)) {
                throw new ResourceConflictException("UnitOfMeasure", "name", request.getName());
            }
        });

        // Check for abbreviation conflict
        unitRepository.findByAbbreviation(request.getAbbreviation()).ifPresent(existingUnit -> {
            if (!existingUnit.getId().equals(unitId)) {
                throw new ResourceConflictException("UnitOfMeasure", "abbreviation", request.getAbbreviation());
            }
        });

        unit.setName(request.getName());
        unit.setAbbreviation(request.getAbbreviation());
        UnitOfMeasure updatedUnit = unitRepository.save(unit);

        auditLogService.logAction(
                securityContextService.getCurrentActor(),
                AuditActionEnum.UPDATE_UNIT_OF_MEASURE,
                "units_of_measure",
                updatedUnit.getId().toString(),
                String.format("Updated unit of measure '%s'", updatedUnit.getName())
        );

        return unitMapper.toUnitOfMeasureResponse(updatedUnit);
    }

    /**
     * Deletes a unit of measure by its ID.
     *
     * @param unitId the ID of the unit of measure to delete
     * @throws ResourceNotFoundException if the unit of measure is not found
     * @throws ResourceConflictException if the unit of measure is in use by any products
     */
    @Transactional
    public void deleteUnit(Integer unitId) {
        UnitOfMeasure unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResourceNotFoundException("UnitOfMeasure", "id", unitId));

        // **Critical Business Rule**: This check now works correctly because of the entity update.
        if (!unit.getProducts().isEmpty()) {
            throw new ResourceConflictException(
                    "Cannot delete unit '" + unit.getName() + "' because it is assigned to " +
                            unit.getProducts().size() + " product(s)."
            );
        }

        auditLogService.logAction(
                securityContextService.getCurrentActor(),
                AuditActionEnum.DELETE_UNIT_OF_MEASURE,
                "units_of_measure",
                unitId.toString(),
                String.format("Deleted unit of measure '%s'", unit.getName())
        );

        unitRepository.delete(unit);
    }
}
