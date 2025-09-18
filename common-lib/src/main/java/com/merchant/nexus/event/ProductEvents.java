package com.merchant.nexus.event;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Product domain events for catalog synchronization.
 * Demonstrates: Event-driven catalog updates, inventory management
 */
public class ProductEvents {

    @Data
    @Builder
    public static class ProductCreated {
        private String productId;
        private String merchantId;
        private String name;
        private String category;
        private BigDecimal price;
        private Integer stockQuantity;
        private Instant createdAt;
    }

    @Data
    @Builder
    public static class ProductUpdated {
        private String productId;
        private String merchantId;
        private String updatedFields;
        private Instant updatedAt;
    }

    @Data
    @Builder
    public static class ProductActivated {
        private String productId;
        private String merchantId;
        private Instant activatedAt;
    }

    @Data
    @Builder
    public static class ProductDeactivated {
        private String productId;
        private String merchantId;
        private String reason;
        private Instant deactivatedAt;
    }

    @Data
    @Builder
    public static class StockUpdated {
        private String productId;
        private String merchantId;
        private Integer previousStock;
        private Integer newStock;
        private String reason;
        private Instant updatedAt;
    }

    @Data
    @Builder
    public static class PriceUpdated {
        private String productId;
        private String merchantId;
        private BigDecimal previousPrice;
        private BigDecimal newPrice;
        private String currency;
        private Instant updatedAt;
    }

    @Data
    @Builder
    public static class ProductDeleted {
        private String productId;
        private String merchantId;
        private String reason;
        private Instant deletedAt;
    }
}
