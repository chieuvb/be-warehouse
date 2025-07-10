package com.example.warehouse.service;

import com.example.warehouse.entity.Product;
import com.example.warehouse.entity.ProductCategory;
import com.example.warehouse.entity.UnitOfMeasure;
import com.example.warehouse.entity.User;
import com.example.warehouse.enums.AuditAction;
import com.example.warehouse.exception.ResourceConflictException;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.mapper.ProductMapper;
import com.example.warehouse.payload.request.ProductRequest;
import com.example.warehouse.payload.response.ProductResponse;
import com.example.warehouse.repository.ProductCategoryRepository;
import com.example.warehouse.repository.ProductRepository;
import com.example.warehouse.repository.UnitOfMeasureRepository;
import com.example.warehouse.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final UnitOfMeasureRepository unitRepository;
    private final ProductMapper productMapper;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toProductResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Integer productId) {
        return productRepository.findById(productId)
                .map(productMapper::toProductResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new ResourceConflictException("Product", "SKU", request.getSku());
        }

        ProductCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", "id", request.getCategoryId()));

        UnitOfMeasure unit = unitRepository.findById(request.getBaseUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("UnitOfMeasure", "id", request.getBaseUnitId()));

        Product product = Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .category(category)
                .baseUnit(unit)
                .minimumStock(request.getMinimumStock())
                .isActive(request.getIsActive())
                .build();

        Product savedProduct = productRepository.save(product);

        auditLogService.logAction(
                getCurrentUser(),
                AuditAction.CREATE_PRODUCT,
                "products",
                savedProduct.getId().toString(),
                String.format("Created product '%s' with SKU '%s'", savedProduct.getName(), savedProduct.getSku())
        );

        return productMapper.toProductResponse(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(Integer productId, ProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Check if SKU is being changed and if the new one conflicts
        productRepository.findBySku(request.getSku()).ifPresent(existingProduct -> {
            if (!existingProduct.getId().equals(productId)) {
                throw new ResourceConflictException("Product", "SKU", request.getSku());
            }
        });

        ProductCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", "id", request.getCategoryId()));

        UnitOfMeasure unit = unitRepository.findById(request.getBaseUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("UnitOfMeasure", "id", request.getBaseUnitId()));

        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(category);
        product.setBaseUnit(unit);
        product.setMinimumStock(request.getMinimumStock());
        product.setIsActive(request.getIsActive());

        Product updatedProduct = productRepository.save(product);

        auditLogService.logAction(
                getCurrentUser(),
                AuditAction.UPDATE_PRODUCT,
                "products",
                updatedProduct.getId().toString(),
                String.format("Updated product '%s'", updatedProduct.getName())
        );

        return productMapper.toProductResponse(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Best practice: Instead of deleting, consider setting isActive to false (soft delete).
        // For this example, we perform a hard delete.
        productRepository.delete(product);

        auditLogService.logAction(
                getCurrentUser(),
                AuditAction.DELETE_PRODUCT,
                "products",
                productId.toString(),
                String.format("Deleted product '%s' with SKU '%s'", product.getName(), product.getSku())
        );
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof SecurityUser) {
            return ((SecurityUser) principal).user();
        }
        // Return null for system actions or if no user is authenticated
        return null;
    }
}
