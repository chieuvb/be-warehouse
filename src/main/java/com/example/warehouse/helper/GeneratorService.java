package com.example.warehouse.helper;

import com.example.warehouse.entity.ProductCategory;
import com.example.warehouse.entity.UnitOfMeasure;
import com.example.warehouse.repository.ProductRepository;
import com.example.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeneratorService {

    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    /**
     * Generates a unique SKU based on product attributes.
     * It creates a base SKU (e.g., "ELE-GAMMOU-PCS") and appends a number
     * if necessary to ensure uniqueness.
     *
     * @param category    The product's category.
     * @param productName The product's name.
     * @param unit        The product's unit of measure.
     * @return A unique, formatted SKU string.
     */
    public String generateSku(ProductCategory category, String productName, UnitOfMeasure unit) {
        String baseSku = createBaseSku(category.getName(), productName, unit.getName());

        // Check if the base SKU itself is unique
        if (!productRepository.existsBySku(baseSku)) {
            return baseSku;
        }

        // If not unique, find the next available numeric suffix
        int counter = 1;
        String nextSku;
        do {
            // Formats the SKU like "BASESKU-001", "BASESKU-002", etc.
            nextSku = String.format("%s-%03d", baseSku, counter++);
        } while (productRepository.existsBySku(nextSku));

        return nextSku;
    }

    /**
     * Creates a sanitized, uppercase base SKU from product attributes.
     * Example: ("Electronics", "Gaming Mouse", "Pieces") -> "ELE-GAMMOU-PCS"
     */
    private String createBaseSku(String categoryName, String productName, String unitName) {
        String catPart = sanitizeAndShorten(categoryName, 3);
        String namePart = sanitizeAndShorten(productName, 6);
        String unitPart = sanitizeAndShorten(unitName, 3);

        return String.format("%s-%s-%s", catPart, namePart, unitPart);
    }

    /**
     * Helper to sanitize, shorten, and uppercase a string part for the SKU.
     */
    private String sanitizeAndShorten(String input, int maxLength) {
        if (input == null || input.isBlank()) {
            return "";
        }
        // Remove all non-alphanumeric characters and convert to uppercase
        String sanitized = input.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        // Return the substring up to the max length
        return sanitized.substring(0, Math.min(sanitized.length(), maxLength));
    }

    /**
     * Generates a unique code for a new warehouse based on its name.
     * It creates a base code (e.g., "MAIN-WH") and appends a number
     * if necessary to ensure uniqueness.
     *
     * @param warehouseName The name of the new warehouse.
     * @return A unique, formatted warehouse code string.
     */
    public String generateWarehouseCode(String warehouseName) {
        String baseCode = createBaseCode(warehouseName);

        // Check if the base code itself is unique
        if (!warehouseRepository.existsByCode(baseCode)) {
            return baseCode;
        }

        // If not unique, find the next available numeric suffix
        int counter = 1;
        String nextCode;
        do {
            // Formats the code like "BASECODE-001", "BASECODE-002", etc.
            nextCode = String.format("%s-%03d", baseCode, counter++);
        } while (warehouseRepository.existsByCode(nextCode));

        return nextCode;
    }

    /**
     * Creates a sanitized, uppercase base code from the warehouse name.
     * Example: ("Main Warehouse North") -> "MAINWHNOR"
     */
    private String createBaseCode(String name) {
        if (name == null || name.isBlank()) {
            // Fallback for empty names, though validation should prevent this.
            return "WH";
        }
        // Remove all non-alphanumeric characters and convert to uppercase
        String sanitized = name.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        // Return the substring up to a reasonable length
        return sanitized.substring(0, Math.min(sanitized.length(), 10));
    }
}
