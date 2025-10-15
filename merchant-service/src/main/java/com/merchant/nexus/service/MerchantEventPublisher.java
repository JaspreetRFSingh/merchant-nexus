package com.merchant.nexus.service;

import com.merchant.nexus.event.DomainEvent;
import com.merchant.nexus.event.MerchantEvents;
import com.merchant.nexus.model.Merchant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka event publisher for merchant domain events.
 * Demonstrates: Event-driven architecture, async messaging, Kafka integration
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MerchantEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String MERCHANT_TOPIC = "merchant-events";

    public void publishMerchantCreated(Merchant merchant) {
        MerchantEvents.MerchantCreated eventData = MerchantEvents.MerchantCreated.builder()
                .merchantId(merchant.getId())
                .businessName(merchant.getBusinessName())
                .email(merchant.getEmail())
                .status(merchant.getStatus().name())
                .createdAt(merchant.getCreatedAt())
                .build();

        DomainEvent<MerchantEvents.MerchantCreated> event = DomainEvent.of(
                "MERCHANT_CREATED",
                merchant.getId(),
                "Merchant",
                eventData
        );

        publishEvent(event);
    }

    public void publishMerchantVerified(Merchant merchant, String verifiedBy) {
        MerchantEvents.MerchantVerified eventData = MerchantEvents.MerchantVerified.builder()
                .merchantId(merchant.getId())
                .verifiedBy(verifiedBy)
                .verifiedAt(merchant.getUpdatedAt())
                .build();

        DomainEvent<MerchantEvents.MerchantVerified> event = DomainEvent.of(
                "MERCHANT_VERIFIED",
                merchant.getId(),
                "Merchant",
                eventData
        );

        publishEvent(event);
    }

    public void publishMerchantSuspended(Merchant merchant, String reason, String suspendedBy) {
        MerchantEvents.MerchantSuspended eventData = MerchantEvents.MerchantSuspended.builder()
                .merchantId(merchant.getId())
                .reason(reason)
                .suspendedBy(suspendedBy)
                .suspendedAt(merchant.getUpdatedAt())
                .build();

        DomainEvent<MerchantEvents.MerchantSuspended> event = DomainEvent.of(
                "MERCHANT_SUSPENDED",
                merchant.getId(),
                "Merchant",
                eventData
        );

        publishEvent(event);
    }

    public void publishMerchantActivated(Merchant merchant, String activatedBy) {
        MerchantEvents.MerchantActivated eventData = MerchantEvents.MerchantActivated.builder()
                .merchantId(merchant.getId())
                .activatedBy(activatedBy)
                .activatedAt(merchant.getUpdatedAt())
                .build();

        DomainEvent<MerchantEvents.MerchantActivated> event = DomainEvent.of(
                "MERCHANT_ACTIVATED",
                merchant.getId(),
                "Merchant",
                eventData
        );

        publishEvent(event);
    }

    public void publishMerchantDeactivated(Merchant merchant, String reason, String deactivatedBy) {
        MerchantEvents.MerchantDeactivated eventData = MerchantEvents.MerchantDeactivated.builder()
                .merchantId(merchant.getId())
                .reason(reason)
                .deactivatedBy(deactivatedBy)
                .deactivatedAt(merchant.getUpdatedAt())
                .build();

        DomainEvent<MerchantEvents.MerchantDeactivated> event = DomainEvent.of(
                "MERCHANT_DEACTIVATED",
                merchant.getId(),
                "Merchant",
                eventData
        );

        publishEvent(event);
    }

    private void publishEvent(DomainEvent<?> event) {
        CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(MERCHANT_TOPIC, event.getAggregateId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug("Event published successfully: {} to topic {} partition {} offset {}",
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
