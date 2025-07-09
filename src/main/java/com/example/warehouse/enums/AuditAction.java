package com.example.warehouse.enums;

/**
 * Enum representing various audit actions in the warehouse management system.
 * Each action corresponds to a specific operation that can be audited.
 */
public enum AuditAction {
    // Inventory actions
    CREATE_INVENTORY("Create Inventory", "Create new inventory record"),
    UPDATE_INVENTORY("Update Inventory", "Update inventory quantity/location"),
    DELETE_INVENTORY("Delete Inventory", "Delete inventory record"),

    // Product actions
    CREATE_PRODUCT("Create Product", "Create new product"),
    UPDATE_PRODUCT("Update Product", "Update product details"),
    DELETE_PRODUCT("Delete Product", "Delete product"),

    // Warehouse actions
    CREATE_WAREHOUSE("Create Warehouse", "Create new warehouse"),
    UPDATE_WAREHOUSE("Update Warehouse", "Update warehouse details"),
    DELETE_WAREHOUSE("Delete Warehouse", "Delete warehouse"),

    // Zone actions
    CREATE_ZONE("Create Zone", "Create warehouse zone"),
    UPDATE_ZONE("Update Zone", "Update zone details"),
    DELETE_ZONE("Delete Zone", "Delete warehouse zone"),

    // User actions
    CREATE_USER("Create User", "Create new user"),
    UPDATE_USER("Update User", "Update user details"),
    DELETE_USER("Delete User", "Delete user"),
    CHANGE_PASSWORD("Change Password", "Change user password");

    private final String displayName;
    private final String description;

    AuditAction(String displayName, String description) {
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
