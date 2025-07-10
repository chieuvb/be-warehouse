package com.example.warehouse.service;

import com.example.warehouse.entity.ProductCategory;
import com.example.warehouse.entity.UnitOfMeasure;
import com.example.warehouse.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SkuGeneratorService {

    private final ProductRepository productRepository;

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
}
