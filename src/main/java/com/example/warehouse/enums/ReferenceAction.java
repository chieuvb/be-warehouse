package com.example.warehouse.enums;

/**
 * Defines the type of source document or entity that a stock transaction is linked to.
 */
public enum ReferenceAction {
    /**
     * Linked to a purchase order for incoming goods.
     */
    PURCHASE_ORDER,

    /**
     * Linked to a sales order for outgoing goods.
     */
    SALES_ORDER,

    /**
     * Linked to an internal work order or manufacturing process.
     */
    WORK_ORDER,

    /**
     * Linked to a customer return authorization (RMA).
     */
    RETURN_AUTHORIZATION,

    /**
     * Linked to a physical stock count or cycle count document.
     */
    STOCK_TAKE_DOCUMENT
}
