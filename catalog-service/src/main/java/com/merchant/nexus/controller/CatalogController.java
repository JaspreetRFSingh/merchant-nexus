package com.merchant.nexus.controller;

import com.merchant.nexus.dto.ProductDTO;
import com.merchant.nexus.exception.ErrorResponse;
import com.merchant.nexus.service.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * REST Controller for Catalog operations.
 */
@RestController
@RequestMapping("${api.base-path}/catalog")
@RequiredArgsConstructor
@Tag(name = "Catalog", description = "Product catalog management APIs")
public class CatalogController {

    private final CatalogService catalogService;

    @PostMapping("/merchants/{merchantId}/products")
    @Operation(summary = "Create a new product", description = "Add a new product to a merchant's catalog")
    @ApiResponse(responseCode = "201", description = "Product created successfully",
                 content = @Content(schema = @Schema(implementation = ProductDTO.ProductResponse.class)))
    public ResponseEntity<ProductDTO.ProductResponse> createProduct(
            @Parameter(description = "Merchant ID") @PathVariable String merchantId,
            @Valid @RequestBody ProductDTO.CreateProductRequest request) {
        ProductDTO.ProductResponse response = catalogService.createProduct(request, merchantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/products/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve product details")
    @ApiResponse(responseCode = "200", description = "Product found")
    public ResponseEntity<ProductDTO.ProductResponse> getProduct(
            @Parameter(description = "Product ID") @PathVariable String id) {
        return ResponseEntity.ok(catalogService.getProductById(id));
    }

    @GetMapping("/merchants/{merchantId}/products")
    @Operation(summary = "List products by merchant", description = "Get all products for a merchant")
    @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    public ResponseEntity<List<ProductDTO.ProductResponse>> getProductsByMerchant(
            @PathVariable String merchantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(catalogService.getProductsByMerchantId(merchantId, page, size));
    }

    @GetMapping("/products/search")
    @Operation(summary = "Search products", description = "Full-text search across product catalog")
    @ApiResponse(responseCode = "200", description = "Search results")
    public ResponseEntity<List<ProductDTO.ProductResponse>> searchProducts(
            @Parameter(description = "Search keyword") @RequestParam(required = false) String keyword,
            @Parameter(description = "Category filter") @RequestParam(required = false) String category,
            @Parameter(description = "Minimum price") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Status filter") @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        return ResponseEntity.ok(catalogService.searchProducts(
                keyword, category, minPrice, maxPrice, status, page, size));
    }

    @PutMapping("/products/{id}")
    @Operation(summary = "Update a product", description = "Update product details")
    @ApiResponse(responseCode = "200", description = "Product updated successfully")
    public ResponseEntity<ProductDTO.ProductResponse> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductDTO.UpdateProductRequest request) {
        return ResponseEntity.ok(catalogService.updateProduct(id, request));
    }

    @PostMapping("/products/{id}/activate")
    @Operation(summary = "Activate a product", description = "Make a product available for sale")
    @ApiResponse(responseCode = "200", description = "Product activated successfully")
    public ResponseEntity<ProductDTO.ProductResponse> activateProduct(
            @PathVariable String id) {
        return ResponseEntity.ok(catalogService.activateProduct(id));
    }

    @PutMapping("/products/{id}/stock")
    @Operation(summary = "Update product stock", description = "Update inventory quantity")
    @ApiResponse(responseCode = "200", description = "Stock updated successfully")
    public ResponseEntity<ProductDTO.ProductResponse> updateStock(
            @PathVariable String id,
            @RequestParam int quantity,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(catalogService.updateStock(id, quantity, reason));
    }

    @DeleteMapping("/products/{id}")
    @Operation(summary = "Delete a product", description = "Remove a product from the catalog")
    @ApiResponse(responseCode = "204", description = "Product deleted successfully")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "No reason provided") String reason) {
        catalogService.deleteProduct(id, reason);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/merchant/{merchantId}")
    @Operation(summary = "Get product count for merchant", description = "Get total product count")
    @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    public ResponseEntity<ProductCountResponse> getProductCount(
            @PathVariable String merchantId) {
        ProductCountResponse response = new ProductCountResponse();
        response.setMerchantId(merchantId);
        response.setTotalCount(catalogService.countByMerchantId(merchantId));
        return ResponseEntity.ok(response);
    }

    @lombok.Data
    public static class ProductCountResponse {
        private String merchantId;
        private long totalCount;
    }
}
