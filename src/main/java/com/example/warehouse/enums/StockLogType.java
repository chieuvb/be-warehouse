package com.example.warehouse.enums;

/**
 * Enum representing various stock log types in the warehouse management system.
 * Each type corresponds to a specific stock operation that can be logged.
 */
public enum StockLogType {
    INITIAL_STOCK("Initial Stock", "Initial stock setup"),
    GOODS_RECEIPT("Goods Receipt", "Stock increase from purchase/transfer in"),
    GOODS_ISSUE("Goods Issue", "Stock decrease from sales/transfer out"),
    ADJUSTMENT_IN("Adjustment In", "Stock increase from inventory adjustment"),
    ADJUSTMENT_OUT("Adjustment Out", "Stock decrease from inventory adjustment"),
    STOCK_COUNT("Stock Count", "Stock update from physical count"),
    RETURN_IN("Return In", "Stock increase from customer return"),
    RETURN_OUT("Return Out", "Stock decrease from supplier return"),
    DAMAGED("Damaged", "Stock decrease from damage/loss"),
    EXPIRED("Expired", "Stock decrease from expiration"),
    RESERVED("Reserved", "Stock reserved for orders");

    private final String displayName;
    private final String description;

    StockLogType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
