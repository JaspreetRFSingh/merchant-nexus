package com.merchant.nexus.event;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

/**
 * Merchant domain events for event-driven architecture.
 * Demonstrates: Event publishing, inter-service communication via Kafka
 */
public class MerchantEvents {

    @Data
    @Builder
    public static class MerchantCreated {
        private String merchantId;
        private String businessName;
        private String email;
        private String status;
        private Instant createdAt;
    }

    @Data
    @Builder
    public static class MerchantVerified {
        private String merchantId;
        private String verifiedBy;
        private Instant verifiedAt;
    }

    @Data
    @Builder
    public static class MerchantSuspended {
        private String merchantId;
        private String reason;
        private String suspendedBy;
        private Instant suspendedAt;
    }

    @Data
    @Builder
    public static class MerchantActivated {
        private String merchantId;
        private String activatedBy;
        private Instant activatedAt;
    }

    @Data
    @Builder
    public static class MerchantDeactivated {
        private String merchantId;
        private String reason;
        private String deactivatedBy;
        private Instant deactivatedAt;
    }

    @Data
    @Builder
    public static class MerchantUpdated {
        private String merchantId;
        private String updatedFields;
        private Instant updatedAt;
    }
}
