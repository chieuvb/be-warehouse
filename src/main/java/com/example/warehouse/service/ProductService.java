package com.example.warehouse.service;

import com.example.warehouse.entity.Product;
import com.example.warehouse.entity.ProductCategory;
import com.example.warehouse.entity.UnitOfMeasure;
import com.example.warehouse.enums.AuditAction;
import com.example.warehouse.exception.ResourceNotFoundException;
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
 * Service class for managing products in the warehouse management system.
 * This service handles CRUD operations, SKU generation, and auditing actions.
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
    private final SkuGeneratorService skuGeneratorService; // Inject the new service

    /**
     * Retrieves all products with pagination support.
     *
     * @param pageable Pagination information
     * @return A paginated list of product responses
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toProductResponse);
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param productId The ID of the product to retrieve
     * @return The product response
     * @throws ResourceNotFoundException if the product does not exist
     */
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Integer productId) {
        return productRepository.findById(productId)
                .map(productMapper::toProductResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
    }

    /**
     * Creates a new product with a unique SKU.
     *
     * @param request The product request containing details for the new product
     * @return The created product response
     * @throws ResourceNotFoundException if the category or unit of measure does not exist
     */
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        // 1. Fetch related entities needed for SKU generation and product creation
        ProductCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", "id", request.getCategoryId()));

        UnitOfMeasure unit = unitRepository.findById(request.getBaseUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("UnitOfMeasure", "id", request.getBaseUnitId()));

        // 2. Generate the unique SKU using the dedicated service
        String generatedSku = skuGeneratorService.generateSku(category, request.getName(), unit);

        // 3. Build the new product with the generated SKU
        Product product = Product.builder()
                .sku(generatedSku)
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
                AuditAction.CREATE_PRODUCT,
                "products",
                savedProduct.getId().toString(),
                String.format("Created product '%s' with SKU '%s'", savedProduct.getName(), savedProduct.getSku())
        );

        return productMapper.toProductResponse(savedProduct);
    }

    /**
     * Updates an existing product, excluding the SKU.
     *
     * @param productId The ID of the product to update
     * @param request   The product request containing updated details
     * @return The updated product response
     * @throws ResourceNotFoundException if the product, category, or unit of measure does not exist
     */
    @Transactional
    public ProductResponse updateProduct(Integer productId, ProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Note: We DO NOT update the SKU. It is immutable.
        // The logic for checking SKU conflicts is removed from the update method.

        ProductCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", "id", request.getCategoryId()));

        UnitOfMeasure unit = unitRepository.findById(request.getBaseUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("UnitOfMeasure", "id", request.getBaseUnitId()));

        // Update all fields EXCEPT the SKU
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(category);
        product.setBaseUnit(unit);
        product.setMinimumStock(request.getMinimumStock());
        product.setIsActive(request.getIsActive());

        Product updatedProduct = productRepository.save(product);

        auditLogService.logAction(
                securityContextService.getCurrentActor(),
                AuditAction.UPDATE_PRODUCT,
                "products",
                updatedProduct.getId().toString(),
                String.format("Updated product '%s'", updatedProduct.getName())
        );

        return productMapper.toProductResponse(updatedProduct);
    }

    /**
     * Deletes a product by its ID.
     *
     * @param productId The ID of the product to delete
     * @throws ResourceNotFoundException if the product does not exist
     */
    @Transactional
    public void deleteProduct(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        productRepository.delete(product);

        auditLogService.logAction(
                securityContextService.getCurrentActor(),
                AuditAction.DELETE_PRODUCT,
                "products",
                productId.toString(),
                String.format("Deleted product '%s' with SKU '%s'", product.getName(), product.getSku())
        );
    }
}
