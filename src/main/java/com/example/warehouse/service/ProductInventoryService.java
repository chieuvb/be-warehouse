package com.example.warehouse.service;

import com.example.warehouse.entity.*;
import com.example.warehouse.enums.ReferenceActionEnum;
import com.example.warehouse.enums.StockLogEnum;
import com.example.warehouse.exception.ResourceConflictException;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.mapper.ProductInventoryMapper;
import com.example.warehouse.mapper.StockLogMapper;
import com.example.warehouse.payload.request.AdjustInventoryRequest;
import com.example.warehouse.payload.request.MoveInventoryRequest;
import com.example.warehouse.payload.response.ProductInventoryResponse;
import com.example.warehouse.payload.response.StockLogResponse;
import com.example.warehouse.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductInventoryService {

    private final ProductInventoryRepository inventoryRepository;
    private final StockLogRepository stockLogRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseZoneRepository zoneRepository;
    private final ProductInventoryMapper inventoryMapper;
    private final StockLogMapper stockLogMapper;
    private final SecurityContextService securityContextService;

    @Transactional(readOnly = true)
    public Page<ProductInventoryResponse> getAllInventory(Pageable pageable) {
        log.info("Retrieving all product inventory records");
        return inventoryRepository.findAll(pageable).map(inventoryMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<StockLogResponse> getStockLogsByInventoryId(Long inventoryId, Pageable pageable) {
        log.info("Retrieving stock logs for inventory ID: {}", inventoryId);
        return stockLogRepository.findByInventoryId(inventoryId, pageable).map(stockLogMapper::toResponse);
    }

    /**
     * Adjusts the quantity of a product in a specific location.
     * This creates a single transaction log (e.g., ADJUSTMENT_IN or ADJUSTMENT_OUT).
     */
    @Transactional
    public ProductInventoryResponse adjustInventory(AdjustInventoryRequest request) {
        ProductInventory inventory = findOrCreateInventory(request.getProductId(), request.getWarehouseId(), request.getZoneId());

        int quantityBefore = inventory.getQuantity();
        int newQuantity = quantityBefore + request.getQuantityChange();

        if (newQuantity < 0) {
            throw new ResourceConflictException("Adjustment would result in negative stock. Current quantity: " + quantityBefore);
        }

        inventory.setQuantity(newQuantity);
        ProductInventory savedInventory = inventoryRepository.save(inventory);

        StockLogEnum type = request.getQuantityChange() > 0 ? StockLogEnum.ADJUSTMENT_IN : StockLogEnum.ADJUSTMENT_OUT;

        logTransaction(
                savedInventory,
                type,
                request.getQuantityChange(),
                quantityBefore,
                request.getNote(),
                request.getReferenceType(),
                request.getReferenceId()
        );

        log.info("Inventory adjusted for product ID: {}, new quantity: {}", request.getProductId(), newQuantity);
        return inventoryMapper.toResponse(savedInventory);
    }

    /**
     * Moves a specified quantity of a product from a source zone to a destination zone.
     * This is an atomic operation that creates two transaction logs (MOVE_OUT and MOVE_IN).
     */
    @Transactional
    public void moveInventory(MoveInventoryRequest request) {
        if (request.getSourceZoneId().equals(request.getDestinationZoneId())) {
            throw new ResourceConflictException("Source and destination zones cannot be the same.");
        }

        // 1. Get source inventory and validate quantity
        ProductInventory sourceInventory = inventoryRepository
                .findByProductIdAndWarehouseIdAndZoneId(request.getProductId(), request.getWarehouseId(), request.getSourceZoneId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found in source zone."));

        if (sourceInventory.getQuantity() < request.getQuantity()) {
            throw new ResourceConflictException("Insufficient stock in source zone. Available: " + sourceInventory.getQuantity());
        }

        // 2. Get or create destination inventory
        ProductInventory destInventory = findOrCreateInventory(request.getProductId(), request.getWarehouseId(), request.getDestinationZoneId());

        // 3. Perform the move on the source
        int sourceQtyBefore = sourceInventory.getQuantity();
        sourceInventory.setQuantity(sourceQtyBefore - request.getQuantity());
        inventoryRepository.save(sourceInventory);
        logTransaction(sourceInventory, StockLogEnum.GOODS_ISSUE, -request.getQuantity(), sourceQtyBefore, request.getNote(),
                ReferenceActionEnum.SALES_ORDER.toString(), destInventory.getId().toString());

        // 4. Perform the move on the destination
        int destQtyBefore = destInventory.getQuantity();
        destInventory.setQuantity(destQtyBefore + request.getQuantity());
        inventoryRepository.save(destInventory);
        logTransaction(destInventory, StockLogEnum.GOODS_RECEIPT, request.getQuantity(), destQtyBefore, request.getNote(),
                ReferenceActionEnum.PURCHASE_ORDER.toString(), destInventory.getId().toString());

        log.info("Moved {} units of product ID: {} from zone ID: {} to zone ID: {}",
                request.getQuantity(), request.getProductId(), request.getSourceZoneId(), request.getDestinationZoneId());
    }

    /**
     * Finds an inventory record or creates a new one with zero quantity if it doesn't exist.
     */
    private ProductInventory findOrCreateInventory(Integer productId, Integer warehouseId, Integer zoneId) {
        return inventoryRepository
                .findByProductIdAndWarehouseIdAndZoneId(productId, warehouseId, zoneId)
                .orElseGet(() -> {
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
                    Warehouse warehouse = warehouseRepository.findById(warehouseId)
                            .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", warehouseId));
                    WarehouseZone zone = zoneRepository.findByWarehouseIdAndId(warehouseId, zoneId)
                            .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", zoneId));

                    ProductInventory newInventory = ProductInventory.builder()
                            .product(product)
                            .warehouse(warehouse)
                            .zone(zone)
                            .quantity(0)
                            .build();
                    return inventoryRepository.save(newInventory);
                });
    }

    /**
     * Creates and saves an immutable StockLog record for any inventory change.
     */
    private void logTransaction(
            ProductInventory inventory,
            StockLogEnum type,
            int quantityChange,
            int quantityBefore,
            String note,
            String referenceAction,
            String referenceId) {
        StockLog stockLog = StockLog.builder()
                .inventory(inventory)
                .actor(securityContextService.getCurrentActor())
                .type(type)
                .quantityBefore(quantityBefore)
                .quantityChange(quantityChange)
                .quantityAfter(inventory.getQuantity())
                .note(note)
                .referenceType(ReferenceActionEnum.valueOf(referenceAction))
                .referenceId(referenceId)
                .build();
        stockLogRepository.save(stockLog);
    }
}
