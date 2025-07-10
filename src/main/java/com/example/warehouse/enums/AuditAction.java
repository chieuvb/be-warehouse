package com.example.warehouse.enums;

import lombok.Getter;

/**
 * Enum representing various audit actions in the warehouse management system.
 * Each action corresponds to a specific operation that can be audited.
 */
@Getter
public enum AuditAction {
    // User actions
    CREATE_USER("Create User", "A new user account was created."),
    UPDATE_USER("Update User", "User details were updated."),
    DELETE_USER("Delete User", "A user account was deleted."),
    CHANGE_PASSWORD("Change Password", "A user changed their password."),
    USER_LOGIN_SUCCESS("User Login Success", "A user successfully logged in."),
    USER_LOGIN_FAILURE("User Login Failure", "A user failed to log in."),

    // Product actions
    CREATE_PRODUCT("Create Product", "A new product was created."),
    UPDATE_PRODUCT("Update Product", "Product details were updated."),
    DELETE_PRODUCT("Delete Product", "A product was deleted."),

    // Inventory actions
    CREATE_INVENTORY("Create Inventory", "A new inventory record was created."),
    UPDATE_INVENTORY("Update Inventory", "Inventory quantity or location was updated."),
    DELETE_INVENTORY("Delete Inventory", "An inventory record was deleted."),

    // Warehouse actions
    CREATE_WAREHOUSE("Create Warehouse", "A new warehouse was created."),
    UPDATE_WAREHOUSE("Update Warehouse", "Warehouse details were updated."),
    DELETE_WAREHOUSE("Delete Warehouse", "A warehouse was deleted."),

    // Zone actions
    CREATE_ZONE("Create Zone", "A new warehouse zone was created."),
    UPDATE_ZONE("Update Zone", "Zone details were updated."),
    DELETE_ZONE("Delete Zone", "A warehouse zone was deleted."),

    OTHER("Other", "An action that does not fit into the predefined categories.");

    private final String displayName;
    private final String description;

    AuditAction(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

}
