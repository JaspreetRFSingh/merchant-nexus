package com.merchant.nexus.model;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Settlement domain model for merchant payouts.
 * Demonstrates: Financial data handling, audit trails, state machines
 */
@Data
@Builder
public class Settlement implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String merchantId;
    private String settlementPeriodStart;
    private String settlementPeriodEnd;
    private BigDecimal grossAmount;
    private BigDecimal commission;
    private BigDecimal tax;
    private BigDecimal adjustment;
    private BigDecimal netAmount;
    private SettlementStatus status;
    private String bankAccountNumber;
    private String bankName;
    private Instant requestedAt;
    private Instant processedAt;
    private String rejectionReason;

    public enum SettlementStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        REJECTED
    }

    public static Settlement createNew(String merchantId, LocalDate periodStart, 
                                        LocalDate periodEnd, BigDecimal grossAmount,
                                        BigDecimal commissionRate, String bankAccountNumber, 
                                        String bankName) {
        BigDecimal commission = grossAmount.multiply(commissionRate);
        BigDecimal tax = commission.multiply(new BigDecimal("0.1")); // 10% VAT on commission
        BigDecimal netAmount = grossAmount.subtract(commission).subtract(tax);

        return Settlement.builder()
                .id(UUID.randomUUID().toString())
                .merchantId(merchantId)
                .settlementPeriodStart(periodStart.toString())
                .settlementPeriodEnd(periodEnd.toString())
                .grossAmount(grossAmount)
                .commission(commission)
                .tax(tax)
                .adjustment(BigDecimal.ZERO)
                .netAmount(netAmount)
                .status(SettlementStatus.PENDING)
                .bankAccountNumber(bankAccountNumber)
                .bankName(bankName)
                .requestedAt(Instant.now())
                .build();
    }

    public void approve() {
        if (this.status != SettlementStatus.PENDING) {
            throw new IllegalStateException("Cannot approve settlement in status: " + this.status);
        }
        this.status = SettlementStatus.PROCESSING;
        this.processedAt = Instant.now();
    }

    public void complete() {
        if (this.status != SettlementStatus.PROCESSING) {
            throw new IllegalStateException("Cannot complete settlement in status: " + this.status);
        }
        this.status = SettlementStatus.COMPLETED;
        this.processedAt = Instant.now();
    }

    public void reject(String reason) {
        this.status = SettlementStatus.REJECTED;
        this.rejectionReason = reason;
        this.processedAt = Instant.now();
    }

    public void applyAdjustment(BigDecimal adjustment) {
        if (this.status != SettlementStatus.PENDING) {
            throw new IllegalStateException("Cannot adjust settlement in status: " + this.status);
        }
        this.adjustment = adjustment;
        this.netAmount = this.netAmount.add(adjustment);
    }
}
