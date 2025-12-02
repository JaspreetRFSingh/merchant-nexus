package com.merchant.nexus.repository;

import com.merchant.nexus.model.Settlement;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * JPA Entity for Settlement - demonstrates financial data modeling, audit trails
 */
@Entity
@Table(name = "settlements")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class SettlementEntity {

    @Id
    private String id;

    @Column(name = "merchant_id", nullable = false, length = 36)
    private String merchantId;

    @Column(name = "settlement_period_start", nullable = false, length = 10)
    private String settlementPeriodStart;

    @Column(name = "settlement_period_end", nullable = false, length = 10)
    private String settlementPeriodEnd;

    @Column(name = "gross_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "commission", nullable = false, precision = 15, scale = 2)
    private BigDecimal commission;

    @Column(name = "tax", nullable = false, precision = 15, scale = 2)
    private BigDecimal tax;

    @Column(name = "adjustment", precision = 15, scale = 2)
    private BigDecimal adjustment;

    @Column(name = "net_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Settlement.SettlementStatus status;

    @Column(name = "bank_account_number", nullable = false, length = 50)
    private String bankAccountNumber;

    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "processed_by", length = 100)
    private String processedBy;

    @CreatedDate
    @Column(name = "requested_at", nullable = false, updatable = false)
    private Instant requestedAt;

    @LastModifiedDate
    @Column(name = "processed_at")
    private Instant processedAt;

    @Version
    private Long version;

    public static SettlementEntity fromDomain(Settlement settlement) {
        SettlementEntity entity = new SettlementEntity();
        entity.setId(settlement.getId());
        entity.setMerchantId(settlement.getMerchantId());
        entity.setSettlementPeriodStart(settlement.getSettlementPeriodStart());
        entity.setSettlementPeriodEnd(settlement.getSettlementPeriodEnd());
        entity.setGrossAmount(settlement.getGrossAmount());
        entity.setCommission(settlement.getCommission());
        entity.setTax(settlement.getTax());
        entity.setAdjustment(settlement.getAdjustment());
        entity.setNetAmount(settlement.getNetAmount());
        entity.setStatus(settlement.getStatus());
        entity.setBankAccountNumber(settlement.getBankAccountNumber());
        entity.setBankName(settlement.getBankName());
        entity.setRejectionReason(settlement.getRejectionReason());
        entity.setRequestedAt(settlement.getRequestedAt());
        entity.setProcessedAt(settlement.getProcessedAt());
        return entity;
    }

    public Settlement toDomain() {
        return Settlement.builder()
                .id(this.id)
                .merchantId(this.merchantId)
                .settlementPeriodStart(this.settlementPeriodStart)
                .settlementPeriodEnd(this.settlementPeriodEnd)
                .grossAmount(this.grossAmount)
                .commission(this.commission)
                .tax(this.tax)
                .adjustment(this.adjustment)
                .netAmount(this.netAmount)
                .status(this.status)
                .bankAccountNumber(this.bankAccountNumber)
                .bankName(this.bankName)
                .rejectionReason(this.rejectionReason)
                .requestedAt(this.requestedAt)
                .processedAt(this.processedAt)
                .build();
    }
}
