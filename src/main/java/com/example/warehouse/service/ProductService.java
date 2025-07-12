package com.example.warehouse.service;

import com.example.warehouse.entity.Product;
import com.example.warehouse.entity.ProductCategory;
import com.example.warehouse.entity.UnitOfMeasure;
import com.example.warehouse.enums.AuditActionEnum;
import com.example.warehouse.exception.ResourceConflictException;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.helper.GeneratorService;
import com.example.warehouse.mapper.ProductMapper;
import com.example.warehouse.payload.request.ProductRequest;
import com.example.warehouse.payload.response.ProductResponse;
import com.example.warehouse.repository.ProductCategoryRepository;
import com.example.warehouse.repository.ProductRepository;
import com.example.warehouse.repository.UnitOfMeasureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing products.
 * Handles business logic for creating, reading, updating, and deleting products,
 * including auto-generation of identifiers and audit logging.
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final UnitOfMeasureRepository unitRepository;
    private final ProductMapper productMapper;
    private final AuditLogService auditLogService;
    private final SecurityContextService securityContextService;
    private final GeneratorService generatorService;

    /**
     * Retrieves a paginated list of all products.
     * @param pageable Pagination and sorting information.
     * @return A page of ProductResponse objects.
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toProductResponse);
    }

    /**
     * Retrieves a single product by its ID.
     * @param productId The ID of the product to retrieve.
     * @return The corresponding ProductResponse.
     * @throws ResourceNotFoundException if no product with the given ID is found.
     */
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Integer productId) {
        return productRepository.findById(productId)
                .map(productMapper::toProductResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
    }

    /**
     * Retrieves a single product by its SKU.
     * @param sku The SKU of the product to retrieve.
     * @return The corresponding ProductResponse.
     * @throws ResourceNotFoundException if no product with the given SKU is found.
     */
    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .map(productMapper::toProductResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "sku", sku));
    }

    /**
     * Retrieves a single product by its barcode.
     * @param barcode The barcode of the product to retrieve.
     * @return The corresponding ProductResponse.
     * @throws ResourceNotFoundException if no product with the given barcode is found.
     */
    @Transactional(readOnly = true)
    public ProductResponse getProductByBarcode(String barcode) {
        return productRepository.findByBarcode(barcode)
                .map(productMapper::toProductResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "barcode", barcode));
    }

    /**
     * Creates a new product with an auto-generated SKU and barcode.
     * @param request The request DTO containing product details.
     * @return The created ProductResponse.
     */
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        // 1. Fetch related entities
        ProductCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", "id", request.getCategoryId()));

        UnitOfMeasure unit = unitRepository.findById(request.getBaseUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("UnitOfMeasure", "id", request.getBaseUnitId()));

        // 2. Generate unique SKU and Barcode
        String generatedSku = generatorService.generateSku(category, request.getName(), unit);
        String generatedBarcode = generatorService.generateEan13Barcode();

        // 3. Build the new product with the generated values
        Product product = Product.builder()
                .sku(generatedSku)
                .barcode(generatedBarcode)
                .name(request.getName())
                .description(request.getDescription())
                .category(category)
                .baseUnit(unit)
                .minimumStock(request.getMinimumStock())
                .isActive(request.getIsActive())
                .build();

        Product savedProduct = productRepository.save(product);

        // 4. Log the creation event
        auditLogService.logAction(
                securityContextService.getCurrentActor(),
                AuditActionEnum.CREATE_PRODUCT,
                "products",
                savedProduct.getId().toString(),
                String.format("Created product '%s' with SKU '%s'", savedProduct.getName(), savedProduct.getSku())
        );

        return productMapper.toProductResponse(savedProduct);
    }

    /**
     * Updates an existing product.
     * Note: The SKU and barcode are immutable and will not be changed.
     * @param productId The ID of the product to update.
     * @param request The request DTO with the new details.
     * @return The updated ProductResponse.
     */
    @Transactional
    public ProductResponse updateProduct(Integer productId, ProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Note: We DO NOT update the SKU or the Barcode. They are immutable.

        ProductCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", "id", request.getCategoryId()));

        UnitOfMeasure unit = unitRepository.findById(request.getBaseUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("UnitOfMeasure", "id", request.getBaseUnitId()));

        // Update all fields EXCEPT the immutable ones
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(category);
        product.setBaseUnit(unit);
        product.setMinimumStock(request.getMinimumStock());
        product.setIsActive(request.getIsActive());

        Product updatedProduct = productRepository.save(product);

        auditLogService.logAction(
                securityContextService.getCurrentActor(),
                AuditActionEnum.UPDATE_PRODUCT,
                "products",
                updatedProduct.getId().toString(),
                String.format("Updated product '%s'", updatedProduct.getName())
        );

        return productMapper.toProductResponse(updatedProduct);
    }

    /**
     * Deletes a product by its ID.
     * This operation is prevented if the product has any existing inventory records.
     * @param productId The ID of the product to delete.
     * @throws ResourceConflictException if the product has associated inventory.
     */
    @Transactional
    public void deleteProduct(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // **Critical Business Rule**: Prevent deletion if the product has inventory.
        if (!product.getInventories().isEmpty()) {
            throw new ResourceConflictException(
                    "Cannot delete product '" + product.getName() + "' because it has " +
                            product.getInventories().size() + " active inventory record(s)."
            );
        }

        auditLogService.logAction(
                securityContextService.getCurrentActor(),
                AuditActionEnum.DELETE_PRODUCT,
                "products",
                productId.toString(),
                String.format("Deleted product '%s' with SKU '%s'", product.getName(), product.getSku())
        );

        productRepository.delete(product);
    }
}
