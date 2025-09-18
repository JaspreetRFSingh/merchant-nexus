package com.merchant.nexus.event;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Settlement domain events for financial processing.
 * Demonstrates: Financial event sourcing, audit trails, saga patterns
 */
public class SettlementEvents {

    @Data
    @Builder
    public static class SettlementRequested {
        private String settlementId;
        private String merchantId;
        private String periodStart;
        private String periodEnd;
        private BigDecimal grossAmount;
        private BigDecimal netAmount;
        private Instant requestedAt;
    }

    @Data
    @Builder
    public static class SettlementApproved {
        private String settlementId;
        private String merchantId;
        private BigDecimal netAmount;
        private String approvedBy;
        private Instant approvedAt;
    }

    @Data
    @Builder
    public static class SettlementRejected {
        private String settlementId;
        private String merchantId;
        private String reason;
        private String rejectedBy;
        private Instant rejectedAt;
    }

    @Data
    @Builder
    public static class SettlementProcessing {
        private String settlementId;
        private String merchantId;
        private String paymentReference;
        private Instant processingAt;
    }

    @Data
    @Builder
    public static class SettlementCompleted {
        private String settlementId;
        private String merchantId;
        private BigDecimal paidAmount;
        private String paymentReference;
        private Instant completedAt;
    }

    @Data
    @Builder
    public static class SettlementFailed {
        private String settlementId;
        private String merchantId;
        private String failureReason;
        private String errorCode;
        private Instant failedAt;
    }

    @Data
    @Builder
    public static class SettlementAdjusted {
        private String settlementId;
        private String merchantId;
        private BigDecimal previousAmount;
        private BigDecimal adjustmentAmount;
        private BigDecimal newAmount;
        private String reason;
        private Instant adjustedAt;
    }
}
