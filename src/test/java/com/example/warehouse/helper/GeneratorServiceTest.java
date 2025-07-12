package com.example.warehouse.helper;

import com.example.warehouse.entity.ProductCategory;
import com.example.warehouse.entity.UnitOfMeasure;
import com.example.warehouse.entity.Warehouse;
import com.example.warehouse.repository.ProductRepository;
import com.example.warehouse.repository.WarehouseRepository;
import com.example.warehouse.repository.WarehouseZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("GeneratorService Tests")
class GeneratorServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private WarehouseZoneRepository warehouseZoneRepository;

    @InjectMocks
    private GeneratorService generatorService;

    private ProductCategory category;
    private UnitOfMeasure unit;
    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        category = new ProductCategory();
        category.setName("Electronics");

        unit = new UnitOfMeasure();
        unit.setAbbreviation("PCS");

        warehouse = new Warehouse();
        warehouse.setCode("WH-MAIN");
    }

    @Test
    @DisplayName("Should generate unique SKU when base SKU is not taken")
    void generateSku_WhenBaseSkuIsUnique_ShouldReturnBaseSku() {
        // Given
        given(productRepository.existsBySku("ELE-GAMEMO-PCS")).willReturn(false);

        // When
        String result = generatorService.generateSku(category, "Game Mouse", unit);

        // Then
        assertThat(result).isEqualTo("ELE-GAMEMO-PCS");
        then(productRepository).should().existsBySku("ELE-GAMEMO-PCS");
    }

    @Test
    @DisplayName("Should generate SKU with suffix when base SKU is taken")
    void generateSku_WhenBaseSkuIsTaken_ShouldReturnSkuWithSuffix() {
        // Given
        given(productRepository.existsBySku("ELE-GAMEMO-PCS")).willReturn(true);
        given(productRepository.existsBySku("ELE-GAMEMO-PCS-001")).willReturn(false);

        // When
        String result = generatorService.generateSku(category, "Game Mouse", unit);

        // Then
        assertThat(result).isEqualTo("ELE-GAMEMO-PCS-001");
        then(productRepository).should().existsBySku("ELE-GAMEMO-PCS");
        then(productRepository).should().existsBySku("ELE-GAMEMO-PCS-001");
    }

    @Test
    @DisplayName("Should generate SKU with incremented suffix when multiple SKUs are taken")
    void generateSku_WhenMultipleSkusAreTaken_ShouldReturnNextAvailableSku() {
        // Given
        given(productRepository.existsBySku("ELE-GAMEMO-PCS")).willReturn(true);
        given(productRepository.existsBySku("ELE-GAMEMO-PCS-001")).willReturn(true);
        given(productRepository.existsBySku("ELE-GAMEMO-PCS-002")).willReturn(true);
        given(productRepository.existsBySku("ELE-GAMEMO-PCS-003")).willReturn(false);

        // When
        String result = generatorService.generateSku(category, "Game Mouse", unit);

        // Then
        assertThat(result).isEqualTo("ELE-GAMEMO-PCS-003");
        then(productRepository).should(times(4)).existsBySku(anyString());
    }

    @Test
    @DisplayName("Should handle special characters in product name")
    void generateSku_WithSpecialCharacters_ShouldSanitizeName() {
        // Given
        given(productRepository.existsBySku("ELE-GAMEMO-PCS")).willReturn(false);

        // When
        String result = generatorService.generateSku(category, "Game-Mouse@#$", unit);

        // Then
        assertThat(result).isEqualTo("ELE-GAMEMO-PCS");
        then(productRepository).should().existsBySku("ELE-GAMEMO-PCS");
    }

    @Test
    @DisplayName("Should handle null or empty product name")
    void generateSku_WithNullProductName_ShouldHandleGracefully() {
        // Given
        given(productRepository.existsBySku("ELE--PCS")).willReturn(false);

        // When
        String result = generatorService.generateSku(category, null, unit);

        // Then
        assertThat(result).isEqualTo("ELE--PCS");
        then(productRepository).should().existsBySku("ELE--PCS");
    }

    @Test
    @DisplayName("Should handle blank product name")
    void generateSku_WithBlankProductName_ShouldHandleGracefully() {
        // Given
        given(productRepository.existsBySku("ELE--PCS")).willReturn(false);

        // When
        String result = generatorService.generateSku(category, "   ", unit);

        // Then
        assertThat(result).isEqualTo("ELE--PCS");
        then(productRepository).should().existsBySku("ELE--PCS");
    }

    @Test
    @DisplayName("Should truncate long product names")
    void generateSku_WithLongProductName_ShouldTruncateToSixCharacters() {
        // Given
        given(productRepository.existsBySku("ELE-GAMING-PCS")).willReturn(false);

        // When
        String result = generatorService.generateSku(category, "GamingMouseProfessional", unit);

        // Then
        assertThat(result).isEqualTo("ELE-GAMING-PCS");
        then(productRepository).should().existsBySku("ELE-GAMING-PCS");
    }

    @Test
    @DisplayName("Should generate unique warehouse code when base code is not taken")
    void generateWarehouseCode_WhenBaseCodeIsUnique_ShouldReturnBaseCode() {
        // Given
        given(warehouseRepository.existsByCode("MAINWAREHO")).willReturn(false);

        // When
        String result = generatorService.generateWarehouseCode("Main Warehouse");

        // Then
        assertThat(result).isEqualTo("MAINWAREHO");
        then(warehouseRepository).should().existsByCode("MAINWAREHO");
    }

    @Test
    @DisplayName("Should generate warehouse code with suffix when base code is taken")
    void generateWarehouseCode_WhenBaseCodeIsTaken_ShouldReturnCodeWithSuffix() {
        // Given
        given(warehouseRepository.existsByCode("MAINWAREHO")).willReturn(true);
        given(warehouseRepository.existsByCode("MAINWAREHO-001")).willReturn(false);

        // When
        String result = generatorService.generateWarehouseCode("Main Warehouse");

        // Then
        assertThat(result).isEqualTo("MAINWAREHO-001");
        then(warehouseRepository).should().existsByCode("MAINWAREHO");
        then(warehouseRepository).should().existsByCode("MAINWAREHO-001");
    }

    @Test
    @DisplayName("Should handle null warehouse name")
    void generateWarehouseCode_WithNullName_ShouldReturnFallback() {
        // Given
        given(warehouseRepository.existsByCode("WH")).willReturn(false);

        // When
        String result = generatorService.generateWarehouseCode(null);

        // Then
        assertThat(result).isEqualTo("WH");
        then(warehouseRepository).should().existsByCode("WH");
    }

    @Test
    @DisplayName("Should handle empty warehouse name")
    void generateWarehouseCode_WithEmptyName_ShouldReturnFallback() {
        // Given
        given(warehouseRepository.existsByCode("WH")).willReturn(false);

        // When
        String result = generatorService.generateWarehouseCode("   ");

        // Then
        assertThat(result).isEqualTo("WH");
        then(warehouseRepository).should().existsByCode("WH");
    }

    @Test
    @DisplayName("Should handle very long warehouse names")
    void generateWarehouseCode_WithVeryLongName_ShouldTruncateToTenCharacters() {
        // Given
        String longName = "VeryLongWarehouseNameThatExceedsTenCharacters";
        given(warehouseRepository.existsByCode("VERYLONGWA")).willReturn(false);

        // When
        String result = generatorService.generateWarehouseCode(longName);

        // Then
        assertThat(result).isEqualTo("VERYLONGWA");
        then(warehouseRepository).should().existsByCode("VERYLONGWA");
    }

    @Test
    @DisplayName("Should generate unique warehouse zone code when base code is not taken")
    void generateWarehouseZoneCode_WhenBaseCodeIsUnique_ShouldReturnBaseCode() {
        // Given
        given(warehouseZoneRepository.existsByCode("WH-MAIN-RECV")).willReturn(false);

        // When
        String result = generatorService.generateWarehouseZoneCode(warehouse, "Receiving");

        // Then
        assertThat(result).isEqualTo("WH-MAIN-RECE");
        then(warehouseZoneRepository).should().existsByCode("WH-MAIN-RECE");
    }

    @Test
    @DisplayName("Should generate warehouse zone code with suffix when base code is taken")
    void generateWarehouseZoneCode_WhenBaseCodeIsTaken_ShouldReturnCodeWithSuffix() {
        // Given
        given(warehouseZoneRepository.existsByCode("WH-MAIN-RECE")).willReturn(true);
        given(warehouseZoneRepository.existsByCode("WH-MAIN-RECE-01")).willReturn(false);

        // When
        String result = generatorService.generateWarehouseZoneCode(warehouse, "Receiving");

        // Then
        assertThat(result).isEqualTo("WH-MAIN-RECE-01");
        then(warehouseZoneRepository).should().existsByCode("WH-MAIN-RECE");
        then(warehouseZoneRepository).should().existsByCode("WH-MAIN-RECE-01");
    }

    @Test
    @DisplayName("Should handle special characters in zone name")
    void generateWarehouseZoneCode_WithSpecialCharacters_ShouldSanitizeZoneName() {
        // Given
        given(warehouseZoneRepository.existsByCode("WH-MAIN-RECE")).willReturn(false);

        // When
        String result = generatorService.generateWarehouseZoneCode(warehouse, "Receiving@#$");

        // Then
        assertThat(result).isEqualTo("WH-MAIN-RECE");
        then(warehouseZoneRepository).should().existsByCode("WH-MAIN-RECE");
    }

    @Test
    @DisplayName("Should truncate long zone names to 4 characters")
    void generateWarehouseZoneCode_WithLongZoneName_ShouldTruncateToFourCharacters() {
        // Given
        given(warehouseZoneRepository.existsByCode("WH-MAIN-RECE")).willReturn(false);

        // When
        String result = generatorService.generateWarehouseZoneCode(warehouse, "ReceivingArea");

        // Then
        assertThat(result).isEqualTo("WH-MAIN-RECE");
        then(warehouseZoneRepository).should().existsByCode("WH-MAIN-RECE");
    }

    @Test
    @DisplayName("Should handle null zone name")
    void generateWarehouseZoneCode_WithNullZoneName_ShouldHandleGracefully() {
        // Given
        given(warehouseZoneRepository.existsByCode("WH-MAIN-")).willReturn(false);

        // When
        String result = generatorService.generateWarehouseZoneCode(warehouse, "");

        // Then
        assertThat(result).isEqualTo("WH-MAIN-");
        then(warehouseZoneRepository).should().existsByCode("WH-MAIN-");
    }

    @Test
    @DisplayName("Should generate unique EAN-13 barcode")
    void generateEan13Barcode_ShouldReturnUniqueThirteenDigitBarcode() {
        // Given
        given(productRepository.existsByBarcode(anyString())).willReturn(false);

        // When
        String result = generatorService.generateEan13Barcode();

        // Then
        assertThat(result)
                .isNotNull()
                .hasSize(13)
                .matches("\\d{13}");
        then(productRepository).should().existsByBarcode(result);
    }

    @Test
    @DisplayName("Should generate another barcode when first one exists")
    void generateEan13Barcode_WhenFirstBarcodeExists_ShouldGenerateAnother() {
        // Given
        given(productRepository.existsByBarcode(anyString()))
                .willReturn(true)   // First barcode exists
                .willReturn(false); // The second barcode doesn't exist

        // When
        String result = generatorService.generateEan13Barcode();

        // Then
        assertThat(result)
                .isNotNull()
                .hasSize(13)
                .matches("\\d{13}");
        then(productRepository).should(times(2)).existsByBarcode(anyString());
    }

    @Test
    @DisplayName("Should generate valid EAN-13 barcode with correct check digit")
    void generateEan13Barcode_ShouldHaveValidCheckDigit() {
        // Given
        given(productRepository.existsByBarcode(anyString())).willReturn(false);

        // When
        String result = generatorService.generateEan13Barcode();

        // Then
        assertThat(result).isNotNull().hasSize(13);

        // Verify the check digit calculation
        String first12Digits = result.substring(0, 12);
        int expectedCheckDigit = calculateExpectedCheckDigit(first12Digits);
        int actualCheckDigit = Character.getNumericValue(result.charAt(12));

        assertThat(actualCheckDigit).isEqualTo(expectedCheckDigit);
        then(productRepository).should().existsByBarcode(result);
    }

    @Test
    @DisplayName("Should handle multiple iterations to find unique barcode")
    void generateEan13Barcode_WithMultipleCollisions_ShouldEventuallyFindUnique() {
        // Given
        given(productRepository.existsByBarcode(anyString()))
                .willReturn(true)   // First collision
                .willReturn(true)   // Second collision
                .willReturn(false); // The third attempt is unique

        // When
        String result = generatorService.generateEan13Barcode();

        // Then
        assertThat(result)
                .isNotNull()
                .hasSize(13)
                .matches("\\d{13}");
        then(productRepository).should(times(3)).existsByBarcode(anyString());
    }

    @Test
    @DisplayName("Should validate EAN-13 check digit calculation edge cases")
    void validateEan13CheckDigitCalculation_EdgeCases() {
        // Test known EAN-13 numbers with their expected check digits
        assertThat(calculateExpectedCheckDigit("123456789012")).isEqualTo(8);
        assertThat(calculateExpectedCheckDigit("000000000000")).isEqualTo(0);
        assertThat(calculateExpectedCheckDigit("999999999999")).isEqualTo(4);
    }

    /**
     * Helper method to calculate an expected EAN-13 check digit for testing
     */
    private int calculateExpectedCheckDigit(String twelveDigits) {
        int sumOdd = 0;
        int sumEven = 0;

        for (int i = 0; i < twelveDigits.length(); i++) {
            int digit = Character.getNumericValue(twelveDigits.charAt(i));
            if ((i + 1) % 2 == 0) {
                sumEven += digit;
            } else {
                sumOdd += digit;
            }
        }

        int totalSum = sumOdd + (sumEven * 3);
        int remainder = totalSum % 10;

        return (remainder == 0) ? 0 : 10 - remainder;
    }
}
