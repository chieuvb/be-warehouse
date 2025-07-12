package com.example.warehouse.service;

import com.example.warehouse.entity.ProductCategory;
import com.example.warehouse.enums.AuditActionEnum;
import com.example.warehouse.exception.ResourceConflictException;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.mapper.ProductCategoryMapper;
import com.example.warehouse.payload.request.ProductCategoryRequest;
import com.example.warehouse.payload.response.ProductCategoryResponse;
import com.example.warehouse.repository.ProductCategoryRepository;
import com.example.warehouse.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing product categories in the warehouse system.
 * Handles creation, retrieval, updating, and deletion of product categories,
 * including validation of category names and parent-child relationships.
 */
@Service
@RequiredArgsConstructor
public class ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductCategoryMapper categoryMapper;
    private final AuditLogService auditLogService;
    private final SecurityContextService securityContextService;

    /**
     * Creates a new product category.
     *
     * @param request The request containing category details
     * @return The created product category response
     */
    @Transactional
    public ProductCategoryResponse createCategory(ProductCategoryRequest request) {
        ProductCategory parent = findParentById(request.getParentId());
        validateNameUniqueness(request.getName(), parent, null);

        ProductCategory category = ProductCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .parentCategory(parent)
                .build();

        ProductCategory savedCategory = categoryRepository.save(category);

        auditLogService.logAction(
                securityContextService.getCurrentActor(),
                AuditActionEnum.CREATE_PRODUCT_CATEGORY,
                "product_categories",
                savedCategory.getId().toString(),
                String.format("Created product category '%s'", savedCategory.getName())
        );

        return categoryMapper.toResponse(savedCategory);
    }

    /**
     * Retrieves the entire category tree, starting from root categories.
     *
     * @return A list of product category responses representing the tree structure
     */
    @Transactional(readOnly = true)
    public List<ProductCategoryResponse> getCategoryTree() {
        List<ProductCategory> rootCategories = categoryRepository.findByParentCategoryIsNull();
        return rootCategories.stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a product category by its ID.
     *
     * @param categoryId The ID of the category to retrieve
     * @return The product category response
     */
    @Transactional(readOnly = true)
    public ProductCategoryResponse getCategoryById(Integer categoryId) {
        return categoryRepository.findById(categoryId)
                .map(categoryMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", "id", categoryId));
    }

    /**
     * Retrieves a product category by its name.
     *
     * @param name The name of the category to retrieve
     * @return The product category response
     */
    @Transactional(readOnly = true)
    public ProductCategoryResponse getCategoryByName(String name) {
        ProductCategory category = categoryRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", "name", name));
        return categoryMapper.toResponse(category);
    }

    /**
     * Updates an existing product category.
     *
     * @param categoryId The ID of the category to update
     * @param request    The request containing updated category details
     * @return The updated product category response
     */
    @Transactional
    public ProductCategoryResponse updateCategory(Integer categoryId, ProductCategoryRequest request) {
        ProductCategory categoryToUpdate = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", "id", categoryId));

        ProductCategory newParent = findParentById(request.getParentId());
        validateNameUniqueness(request.getName(), newParent, categoryId);

        if (newParent != null) {
            checkCircularDependency(categoryToUpdate, newParent);
        }

        categoryToUpdate.setName(request.getName());
        categoryToUpdate.setDescription(request.getDescription());
        categoryToUpdate.setParentCategory(newParent);

        ProductCategory updatedCategory = categoryRepository.save(categoryToUpdate);

        auditLogService.logAction(
                securityContextService.getCurrentActor(),
                AuditActionEnum.UPDATE_PRODUCT_CATEGORY,
                "product_categories",
                updatedCategory.getId().toString(),
                String.format("Updated product category '%s'", updatedCategory.getName())
        );

        return categoryMapper.toResponse(updatedCategory);
    }

    /**
     * Deletes a product category by its ID.
     *
     * @param categoryId The ID of the category to delete
     */
    @Transactional
    public void deleteCategory(Integer categoryId) {
        ProductCategory categoryToDelete = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", "id", categoryId));

        if (productRepository.existsByCategoryId(categoryId)) {
            throw new ResourceConflictException("Cannot delete category: It is assigned to one or more products.");
        }

        // The database schema's `ON DELETE SET NULL` for the foreign key
        // will automatically handle making children top-level.

        auditLogService.logAction(
                securityContextService.getCurrentActor(),
                AuditActionEnum.DELETE_PRODUCT_CATEGORY,
                "product_categories",
                categoryId.toString(),
                String.format("Deleted product category '%s'", categoryToDelete.getName())
        );

        categoryRepository.delete(categoryToDelete);
    }

    /**
     * Finds a parent category by its ID.
     *
     * @param parentId The ID of the parent category
     * @return The parent product category, or null if no parent is specified
     */
    private ProductCategory findParentById(Integer parentId) {
        if (parentId == null) {
            return null;
        }
        return categoryRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent Category", "id", parentId));
    }

    /**
     * Validates that the category name is unique within its parent category.
     *
     * @param name        The name of the category to validate
     * @param parent      The parent category, or null for top-level categories
     * @param categoryId  The ID of the category being updated, or null for new categories
     */
    private void validateNameUniqueness(String name, ProductCategory parent, Integer categoryId) {
        boolean exists;
        if (parent == null) {
            exists = (categoryId == null)
                    ? categoryRepository.existsByNameAndParentCategoryIsNull(name)
                    : categoryRepository.existsByNameAndParentCategoryIsNullAndIdNot(name, categoryId);
        } else {
            exists = (categoryId == null)
                    ? categoryRepository.existsByNameAndParentCategory(name, parent)
                    : categoryRepository.existsByNameAndParentCategoryAndIdNot(name, parent, categoryId);
        }
        if (exists) {
            throw new ResourceConflictException("A category with this name already exists at this level.");
        }
    }

    /**
     * Checks for circular dependencies when moving a category under a new parent.
     *
     * @param category         The category being moved
     * @param potentialParent  The new parent category
     * @throws ResourceConflictException if a circular dependency is detected
     */
    private void checkCircularDependency(ProductCategory category, ProductCategory potentialParent) {
        if (potentialParent.getId().equals(category.getId())) {
            throw new ResourceConflictException("A category cannot be its own parent.");
        }
        ProductCategory current = potentialParent;
        while (current != null) {
            if (current.getId().equals(category.getId())) {
                throw new ResourceConflictException("Circular dependency detected: you cannot move a category under one of its own descendants.");
            }
            current = current.getParentCategory();
        }
    }
}
