package com.example.warehouse.enums;

import lombok.Getter;

/**
 * Enum representing different roles in the warehouse management system.
 * Each role has a display name and a description for clarity.
 */
@Getter
public enum RoleType {
    ROLE_ADMIN("Admin", "System administrator with full access."),
    ROLE_WAREHOUSE("Warehouse Staff", "Handles stock in/out and inventory checks."),
    ROLE_WAREHOUSE_MANAGER("Warehouse Manager", "Oversees warehouse operations and approvals."),
    ROLE_SALES("Sales Staff", "Creates sales orders and checks stock availability."),
    ROLE_ACCOUNTANT("Accountant", "Manages financial records related to warehouse operations."),
    ROLE_DELIVERY("Delivery Staff", "Handles order deliveries and updates shipment status."),
    ROLE_VIEWER("Viewer", "Has read-only access."),
    ROLE_INTERN("Intern", "Limited access for interns.");

    private final String displayName;
    private final String description;

    RoleType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
