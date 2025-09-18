package com.merchant.nexus.dto;

import com.merchant.nexus.model.Product;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * DTOs for Product/Catalog API
 */
public class ProductDTO {

    @Data
    @Builder
    public static class CreateProductRequest {
        @NotBlank(message = "Product name is required")
        private String name;

        private String description;

        @NotBlank(message = "Category is required")
        private String category;

        private List<String> tags;

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        private BigDecimal priceAmount;

        private String currency;

        @NotNull(message = "Cost price is required")
        @Positive(message = "Cost price must be positive")
        private BigDecimal costPriceAmount;

        @NotNull(message = "Stock quantity is required")
        @Positive(message = "Stock quantity must be non-negative")
        private Integer stockQuantity;

        private String imageUrl;
        private List<String> imageUrls;
    }

    @Data
    @Builder
    public static class UpdateProductRequest {
        private String name;
        private String description;
        private String category;
        private List<String> tags;
        private BigDecimal priceAmount;
        private String currency;
        private BigDecimal costPriceAmount;
        private Integer stockQuantity;
        private String imageUrl;
        private List<String> imageUrls;
    }

    @Data
    @Builder
    public static class ProductResponse {
        private String id;
        private String merchantId;
        private String name;
        private String description;
        private String category;
        private List<String> tags;
        private MoneyResponse price;
        private MoneyResponse costPrice;
        private Integer stockQuantity;
        private String imageUrl;
        private List<String> imageUrls;
        private String status;
        @JsonProperty("createdAt")
        private Instant createdAt;
        @JsonProperty("updatedAt")
        private Instant updatedAt;
    }

    @Data
    @Builder
    public static class MoneyResponse {
        private BigDecimal amount;
        private String currency;
    }

    @Data
    @Builder
    public static class ProductListResponse {
        private List<ProductResponse> products;
        private long total;
        private int page;
        private int size;
    }

    @Data
    @Builder
    public static class SearchCriteria {
        private String keyword;
        private String category;
        private List<String> categories;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private String status;
        private Integer page;
        private Integer size;
        private String sortBy;
        private String sortOrder;
    }

    public static ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .merchantId(product.getMerchantId())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .tags(product.getTags())
                .price(product.getPrice() != null ? 
                    MoneyResponse.builder()
                        .amount(product.getPrice().getAmount())
                        .currency(product.getPrice().getCurrency())
                        .build() : null)
                .costPrice(product.getCostPrice() != null ?
                    MoneyResponse.builder()
                        .amount(product.getCostPrice().getAmount())
                        .currency(product.getCostPrice().getCurrency())
                        .build() : null)
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .imageUrls(product.getImageUrls())
                .status(product.getStatus() != null ? product.getStatus().name() : null)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
