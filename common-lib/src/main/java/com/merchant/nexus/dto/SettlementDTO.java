package com.merchant.nexus.dto;

import com.merchant.nexus.model.Settlement;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * DTOs for Settlement API - demonstrates financial data handling
 */
public class SettlementDTO {

    @Data
    @Builder
    public static class CreateSettlementRequest {
        @NotBlank(message = "Merchant ID is required")
        private String merchantId;

        @NotBlank(message = "Period start is required")
        private String periodStart;

        @NotBlank(message = "Period end is required")
        private String periodEnd;

        @NotBlank(message = "Bank account number is required")
        private String bankAccountNumber;

        @NotBlank(message = "Bank name is required")
        private String bankName;
    }

    @Data
    @Builder
    public static class SettlementResponse {
        private String id;
        private String merchantId;
        private String settlementPeriodStart;
        private String settlementPeriodEnd;
        private BigDecimal grossAmount;
        private BigDecimal commission;
        private BigDecimal tax;
        private BigDecimal adjustment;
        private BigDecimal netAmount;
        private String status;
        private String bankAccountNumber;
        private String bankName;
        @JsonProperty("requestedAt")
        private Instant requestedAt;
        @JsonProperty("processedAt")
        private Instant processedAt;
        private String rejectionReason;
    }

    @Data
    @Builder
    public static class SettlementListResponse {
        private List<SettlementResponse> settlements;
        private long total;
        private int page;
        private int size;
    }

    @Data
    @Builder
    public static class ApproveSettlementRequest {
        private String approvedBy;
    }

    @Data
    @Builder
    public static class RejectSettlementRequest {
        @NotBlank(message = "Rejection reason is required")
        private String reason;
    }

    @Data
    @Builder
    public static class AdjustmentRequest {
        @NotBlank(message = "Adjustment amount is required")
        private BigDecimal amount;

        private String reason;
    }

    @Data
    @Builder
    public static class SettlementSummary {
        private String merchantId;
        private BigDecimal totalGrossAmount;
        private BigDecimal totalCommission;
        private BigDecimal totalTax;
        private BigDecimal totalNetAmount;
        private int pendingCount;
        private int processingCount;
        private int completedCount;
        private int failedCount;
    }

    public static SettlementResponse toResponse(Settlement settlement) {
        return SettlementResponse.builder()
                .id(settlement.getId())
                .merchantId(settlement.getMerchantId())
                .settlementPeriodStart(settlement.getSettlementPeriodStart())
                .settlementPeriodEnd(settlement.getSettlementPeriodEnd())
                .grossAmount(settlement.getGrossAmount())
                .commission(settlement.getCommission())
                .tax(settlement.getTax())
                .adjustment(settlement.getAdjustment())
                .netAmount(settlement.getNetAmount())
                .status(settlement.getStatus().name())
                .bankAccountNumber(settlement.getBankAccountNumber())
                .bankName(settlement.getBankName())
                .requestedAt(settlement.getRequestedAt())
                .processedAt(settlement.getProcessedAt())
                .rejectionReason(settlement.getRejectionReason())
                .build();
    }
}
