package com.example.warehouse.controller;

import com.example.warehouse.payload.request.ProductRequest;
import com.example.warehouse.payload.response.ApiResponse;
import com.example.warehouse.payload.response.ProductResponse;
import com.example.warehouse.service.ProductService;
import com.example.warehouse.utility.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing products in the warehouse management system.
 * Provides endpoints for CRUD operations on products.
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ProductController {

    private final ProductService productService;

    /**
     * Retrieves all products with pagination support.
     *
     * @param pageable Pagination information
     * @return A paginated list of product responses
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
            @PageableDefault(sort = "name") Pageable pageable) {
        Page<ProductResponse> products = productService.getAllProducts(pageable);
        return ResponseUtil.createSuccessResponse("Products retrieved successfully", products);
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param id The ID of the product to retrieve
     * @return The product response
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Integer id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseUtil.createSuccessResponse("Product retrieved successfully", product);
    }

    /**
     * Retrieves a product by its SKU.
     *
     * @param sku The SKU of the product to retrieve
     * @return The product response
     */
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductBySku(@PathVariable String sku) {
        ProductResponse product = productService.getProductBySku(sku);
        return ResponseUtil.createSuccessResponse("Product retrieved successfully", product);
    }

    /**
     * Creates a new product.
     *
     * @param request The request payload containing product information
     * @return The created product response
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse newProduct = productService.createProduct(request);
        ApiResponse<ProductResponse> response = ApiResponse.success("Product created successfully", newProduct);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Updates an existing product by its ID.
     *
     * @param id      The ID of the product to update
     * @param request The request payload containing updated product information
     * @return The updated product response
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Integer id,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse updatedProduct = productService.updateProduct(id, request);
        return ResponseUtil.createSuccessResponse("Product updated successfully", updatedProduct);
    }

    /**
     * Deletes a product by its ID.
     *
     * @param id The ID of the product to delete
     * @return A response indicating the deletion was successful
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Integer id) {
        productService.deleteProduct(id);
        return ResponseUtil.createSuccessResponse("Product deleted successfully", null);
    }
}
