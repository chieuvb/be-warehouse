package com.example.warehouse.enums;

import lombok.Getter;

/**
 * Enum representing various stock log types in the warehouse management system.
 * Each type corresponds to a specific stock operation that can be logged.
 */
@Getter
public enum StockLogEnum {
    INITIAL_STOCK("Initial Stock", "Initial stock quantity set up for a new product."),
    GOODS_RECEIPT("Goods Receipt", "Stock increase from a purchase order or transfer in."),
    GOODS_ISSUE("Goods Issue", "Stock decrease from a sales order or transfer out."),
    ADJUSTMENT_IN("Adjustment In", "Stock increase from a manual inventory adjustment."),
    ADJUSTMENT_OUT("Adjustment Out", "Stock decrease from a manual inventory adjustment."),
    STOCK_COUNT("Stock Count", "Stock level updated after a physical inventory count."),
    RETURN_IN("Return In", "Stock increase from a customer return."),
    RETURN_OUT("Return Out", "Stock decrease from a return to a supplier."),
    DAMAGED("Damaged", "Stock decrease due to damaged or lost goods."),
    EXPIRED("Expired", "Stock decrease due to product expiration."),
    RESERVED("Reserved", "Stock allocated for an open order, not physically moved yet.");

    private final String displayName;
    private final String description;

    StockLogEnum(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

}
