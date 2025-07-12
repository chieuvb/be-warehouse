package com.example.warehouse.controller;

import com.example.warehouse.payload.request.ProductCategoryRequest;
import com.example.warehouse.payload.response.ApiResponse;
import com.example.warehouse.payload.response.ProductCategoryResponse;
import com.example.warehouse.service.ProductCategoryService;
import com.example.warehouse.utility.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing product categories in the warehouse management system.
 * Provides endpoints for creating, retrieving, updating, and deleting product categories.
 */
@RestController
@RequestMapping("/product-categories")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ProductCategoryController {

    private final ProductCategoryService categoryService;

    /**
     * Creates a new product category.
     *
     * @param request The request containing category details
     * @return The created product category response
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductCategoryResponse>> createCategory(@Valid @RequestBody ProductCategoryRequest request) {
        ProductCategoryResponse newCategory = categoryService.createCategory(request);
        ApiResponse<ProductCategoryResponse> response = ApiResponse.success("Product category created successfully", newCategory);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves all product categories with pagination support.
     *
     * @return A list of product category responses
     */
    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<ProductCategoryResponse>>> getCategoryTree() {
        List<ProductCategoryResponse> categoryTree = categoryService.getCategoryTree();
        return ResponseUtil.createSuccessResponse("Category tree retrieved successfully", categoryTree);
    }

    /**
     * Retrieves a product category by its ID.
     *
     * @param id The ID of the category to retrieve
     * @return The product category response
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductCategoryResponse>> getCategoryById(@PathVariable Integer id) {
        ProductCategoryResponse category = categoryService.getCategoryById(id);
        return ResponseUtil.createSuccessResponse("Category retrieved successfully", category);
    }

    /**
     * Retrieves a product category by its name.
     *
     * @param name The name of the category to retrieve
     * @return The product category response
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<ProductCategoryResponse>> getCategoryByName(String name) {
        ProductCategoryResponse category = categoryService.getCategoryByName(name);
        return ResponseUtil.createSuccessResponse("All categories retrieved successfully", category);
    }

    /**
     * Updates an existing product category.
     *
     * @param id      The ID of the category to update
     * @param request The request containing updated category details
     * @return The updated product category response
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductCategoryResponse>> updateCategory(
            @PathVariable Integer id,
            @Valid @RequestBody ProductCategoryRequest request) {
        ProductCategoryResponse updatedCategory = categoryService.updateCategory(id, request);
        return ResponseUtil.createSuccessResponse("Category updated successfully", updatedCategory);
    }

    /**
     * Deletes a product category by its ID.
     *
     * @param id The ID of the category to delete
     * @return A response indicating the deletion was successful
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
        return ResponseUtil.createSuccessResponse("Category deleted successfully", null);
    }
}
