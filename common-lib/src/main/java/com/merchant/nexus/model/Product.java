package com.merchant.nexus.model;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Product domain model for the catalog system.
 * Demonstrates: Value objects, money handling, categorization
 */
@Data
@Builder
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String merchantId;
    private String name;
    private String description;
    private String category;
    private List<String> tags;
    private Money price;
    private Money costPrice;
    private Integer stockQuantity;
    private String imageUrl;
    private List<String> imageUrls;
    private ProductStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public enum ProductStatus {
        DRAFT,
        ACTIVE,
        OUT_OF_STOCK,
        DISCONTINUED
    }

    @Data
    @Builder
    public static class Money implements Serializable {
        private BigDecimal amount;
        private String currency;

        public static Money of(BigDecimal amount, String currency) {
            return Money.builder().amount(amount).currency(currency).build();
        }

        public static Money ofKRW(BigDecimal amount) {
            return Money.builder().amount(amount).currency("KRW").build();
        }
    }

    public static Product createNew(String merchantId, String name, String category, 
                                     Money price, Integer stockQuantity) {
        return Product.builder()
                .id(UUID.randomUUID().toString())
                .merchantId(merchantId)
                .name(name)
                .category(category)
                .price(price)
                .stockQuantity(stockQuantity)
                .status(ProductStatus.DRAFT)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public void activate() {
        if (stockQuantity > 0) {
            this.status = ProductStatus.ACTIVE;
        } else {
            this.status = ProductStatus.OUT_OF_STOCK;
        }
        this.updatedAt = Instant.now();
    }

    public void updateStock(int quantity) {
        this.stockQuantity = quantity;
        if (quantity <= 0) {
            this.status = ProductStatus.OUT_OF_STOCK;
        } else if (this.status == ProductStatus.OUT_OF_STOCK) {
            this.status = ProductStatus.ACTIVE;
        }
        this.updatedAt = Instant.now();
    }
}
