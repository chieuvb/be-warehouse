package com.example.warehouse.controller;

import com.example.warehouse.payload.request.AdjustInventoryRequest;
import com.example.warehouse.payload.request.MoveInventoryRequest;
import com.example.warehouse.payload.response.ApiResponse;
import com.example.warehouse.payload.response.ProductInventoryResponse;
import com.example.warehouse.payload.response.StockLogResponse;
import com.example.warehouse.service.ProductInventoryService;
import com.example.warehouse.utility.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing product inventory in the warehouse management system.
 * Provides endpoints for adjusting inventory, moving inventory, and retrieving inventory history.
 */
@RestController
@RequestMapping("/inventories")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
public class InventoryController {

    private final ProductInventoryService inventoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductInventoryResponse>>> getAllInventory(
            @PageableDefault(sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProductInventoryResponse> inventoryPage = inventoryService.getAllInventory(pageable);
        return ResponseUtil.createSuccessResponse("Inventory retrieved successfully", inventoryPage);
    }

    @PostMapping("/adjust")
    public ResponseEntity<ApiResponse<ProductInventoryResponse>> adjustInventory(
            @Valid @RequestBody AdjustInventoryRequest request) {
        ProductInventoryResponse updatedInventory = inventoryService.adjustInventory(request);
        return ResponseUtil.createSuccessResponse("Inventory adjusted successfully", updatedInventory);
    }

    @PostMapping("/move")
    public ResponseEntity<ApiResponse<Void>> moveInventory(
            @Valid @RequestBody MoveInventoryRequest request) {
        inventoryService.moveInventory(request);
        return ResponseUtil.createSuccessResponse("Inventory moved successfully", null);
    }

    @GetMapping("/{inventoryId}/history")
    public ResponseEntity<ApiResponse<Page<StockLogResponse>>> getInventoryHistory(
            @PathVariable Long inventoryId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<StockLogResponse> historyPage = inventoryService.getStockLogsByInventoryId(inventoryId, pageable);
        return ResponseUtil.createSuccessResponse("Inventory history retrieved successfully", historyPage);
    }
}
