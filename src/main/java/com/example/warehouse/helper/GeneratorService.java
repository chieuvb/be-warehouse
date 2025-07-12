package com.example.warehouse.helper;

import com.example.warehouse.entity.ProductCategory;
import com.example.warehouse.entity.UnitOfMeasure;
import com.example.warehouse.entity.Warehouse;
import com.example.warehouse.repository.ProductRepository;
import com.example.warehouse.repository.WarehouseRepository;
import com.example.warehouse.repository.WarehouseZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class GeneratorService {

    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseZoneRepository warehouseZoneRepository;

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
        String baseSku = createBaseSku(category.getName(), productName, unit.getAbbreviation());

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
     * Generates a unique code for a warehouse zone based on the parent warehouse and zone name.
     * Example: Warehouse "WH-MAIN", Zone "Receiving" -> "WH-MAIN-RECV"
     *
     * @param warehouse The parent warehouse entity.
     * @param zoneName The name of the new zone.
     * @return A unique, formatted warehouse zone code.
     */
    public String generateWarehouseZoneCode(Warehouse warehouse, String zoneName) {
        String baseCode = createBaseZoneCode(warehouse.getCode(), zoneName);

        if (!warehouseZoneRepository.existsByCode(baseCode)) {
            return baseCode;
        }

        // If not unique, find the next available numeric suffix
        int counter = 1;
        String nextCode;
        do {
            nextCode = String.format("%s-%02d", baseCode, counter++);
        } while (warehouseZoneRepository.existsByCode(nextCode));

        return nextCode;
    }

    /**
     * Generates a unique 13-digit EAN-style barcode.
     * It creates a random 12-digit base and calculates the 13th digit as a checksum,
     * ensuring the final barcode is unique in the database.
     *
     * @return A unique 13-digit barcode string.
     */
    public String generateEan13Barcode() {
        String fullBarcode;
        // Use a do-while loop to guarantee uniqueness, though collisions are extremely rare.
        do {
            // Generate a random 12-digit number as the base.
            long randomBase = ThreadLocalRandom.current().nextLong(100_000_000_000L, 1_000_000_000_000L);
            String twelveDigits = String.valueOf(randomBase);

            // Calculate the EAN-13 check digit.
            int checkDigit = calculateEan13CheckDigit(twelveDigits);
            fullBarcode = twelveDigits + checkDigit;

        } while (productRepository.existsByBarcode(fullBarcode));

        return fullBarcode;
    }

    /**
     * Calculates the check digit for an EAN-13 barcode.
     *
     * @param twelveDigits The first 12 digits of the barcode.
     * @return The calculated 13th check digit.
     */
    private int calculateEan13CheckDigit(String twelveDigits) {
        int sumOdd = 0;
        int sumEven = 0;

        for (int i = 0; i < twelveDigits.length(); i++) {
            int digit = Character.getNumericValue(twelveDigits.charAt(i));
            if ((i + 1) % 2 == 0) { // Even position (2nd, 4th, ...)
                sumEven += digit;
            } else { // Odd position (1st, 3rd, ...)
                sumOdd += digit;
            }
        }

        int totalSum = sumOdd + (sumEven * 3);
        int remainder = totalSum % 10;

        return (remainder == 0) ? 0 : 10 - remainder;
    }

    /**
     * Creates a sanitized, uppercase base code for a warehouse zone.
     * Example: ("WH-MAIN", "Receiving") -> "WH-MAIN-RECV"
     */
    private String createBaseZoneCode(String warehouseCode, String zoneName) {
        String zonePart = zoneName.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        zonePart = zonePart.substring(0, Math.min(zonePart.length(), 4));
        return String.format("%s-%s", warehouseCode, zonePart);
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
