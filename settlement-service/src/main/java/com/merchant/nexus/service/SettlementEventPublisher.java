package com.merchant.nexus.service;

import com.merchant.nexus.event.DomainEvent;
import com.merchant.nexus.event.SettlementEvents;
import com.merchant.nexus.model.Settlement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka event publisher for settlement domain events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String SETTLEMENT_TOPIC = "settlement-events";

    public void publishSettlementRequested(Settlement settlement) {
        SettlementEvents.SettlementRequested eventData = SettlementEvents.SettlementRequested.builder()
                .settlementId(settlement.getId())
                .merchantId(settlement.getMerchantId())
                .periodStart(settlement.getSettlementPeriodStart())
                .periodEnd(settlement.getSettlementPeriodEnd())
                .grossAmount(settlement.getGrossAmount())
                .netAmount(settlement.getNetAmount())
                .requestedAt(settlement.getRequestedAt())
                .build();

        DomainEvent<SettlementEvents.SettlementRequested> event = DomainEvent.of(
                "SETTLEMENT_REQUESTED",
                settlement.getId(),
                "Settlement",
                eventData
        );

        publishEvent(event);
    }

    public void publishSettlementApproved(Settlement settlement, String approvedBy) {
        SettlementEvents.SettlementApproved eventData = SettlementEvents.SettlementApproved.builder()
                .settlementId(settlement.getId())
                .merchantId(settlement.getMerchantId())
                .netAmount(settlement.getNetAmount())
                .approvedBy(approvedBy)
                .approvedAt(settlement.getProcessedAt())
                .build();

        DomainEvent<SettlementEvents.SettlementApproved> event = DomainEvent.of(
                "SETTLEMENT_APPROVED",
                settlement.getId(),
                "Settlement",
                eventData
        );

        publishEvent(event);
    }

    public void publishSettlementRejected(Settlement settlement, String reason, String rejectedBy) {
        SettlementEvents.SettlementRejected eventData = SettlementEvents.SettlementRejected.builder()
                .settlementId(settlement.getId())
                .merchantId(settlement.getMerchantId())
                .reason(reason)
                .rejectedBy(rejectedBy)
                .rejectedAt(settlement.getProcessedAt())
                .build();

        DomainEvent<SettlementEvents.SettlementRejected> event = DomainEvent.of(
                "SETTLEMENT_REJECTED",
                settlement.getId(),
                "Settlement",
                eventData
        );

        publishEvent(event);
    }

    public void publishSettlementCompleted(Settlement settlement, String paymentReference) {
        SettlementEvents.SettlementCompleted eventData = SettlementEvents.SettlementCompleted.builder()
                .settlementId(settlement.getId())
                .merchantId(settlement.getMerchantId())
                .paidAmount(settlement.getNetAmount())
                .paymentReference(paymentReference)
                .completedAt(settlement.getProcessedAt())
                .build();

        DomainEvent<SettlementEvents.SettlementCompleted> event = DomainEvent.of(
                "SETTLEMENT_COMPLETED",
                settlement.getId(),
                "Settlement",
                eventData
        );

        publishEvent(event);
    }

    public void publishSettlementAdjusted(Settlement settlement, BigDecimal previousAmount, 
                                           BigDecimal adjustmentAmount, String reason) {
        SettlementEvents.SettlementAdjusted eventData = SettlementEvents.SettlementAdjusted.builder()
                .settlementId(settlement.getId())
                .merchantId(settlement.getMerchantId())
                .previousAmount(previousAmount)
                .adjustmentAmount(adjustmentAmount)
                .newAmount(settlement.getNetAmount())
                .reason(reason)
                .adjustedAt(settlement.getProcessedAt())
                .build();

        DomainEvent<SettlementEvents.SettlementAdjusted> event = DomainEvent.of(
                "SETTLEMENT_ADJUSTED",
                settlement.getId(),
                "Settlement",
                eventData
        );

        publishEvent(event);
    }

    private void publishEvent(DomainEvent<?> event) {
        CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(SETTLEMENT_TOPIC, event.getAggregateId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug("Event published: {} to {} partition {} offset {}",
                        event.getEventType(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish event: {}", event.getEventType(), ex);
            }
        });
    }
}
