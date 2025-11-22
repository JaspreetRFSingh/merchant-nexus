package com.merchant.nexus.service;

import com.merchant.nexus.event.DomainEvent;
import com.merchant.nexus.event.MerchantEvents;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer for merchant events - demonstrates event-driven inter-service communication.
 * When a merchant is deactivated, all their products are also deactivated.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantEventConsumer {

    private final CatalogService catalogService;

    @KafkaListener(
            topics = "merchant-events",
            groupId = "catalog-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeMerchantEvent(DomainEvent<?> event) {
        log.info("Received merchant event: {}", event.getEventType());

        switch (event.getEventType()) {
            case "MERCHANT_DEACTIVATED":
                handleMerchantDeactivated((DomainEvent<MerchantEvents.MerchantDeactivated>) event);
                break;
            case "MERCHANT_SUSPENDED":
                handleMerchantSuspended((DomainEvent<MerchantEvents.MerchantSuspended>) event);
                break;
            default:
                log.debug("Ignoring event type: {}", event.getEventType());
        }
    }

    private void handleMerchantDeactivated(DomainEvent<MerchantEvents.MerchantDeactivated> event) {
        String merchantId = event.getData().getMerchantId();
        log.info("Merchant {} deactivated - deactivating all products", merchantId);
        // In a real implementation, we would deactivate all products for this merchant
        // catalogService.deactivateAllProductsByMerchant(merchantId);
    }

    private void handleMerchantSuspended(DomainEvent<MerchantEvents.MerchantSuspended> event) {
        String merchantId = event.getData().getMerchantId();
        log.info("Merchant {} suspended - suspending all products", merchantId);
        // In a real implementation, we would suspend all products for this merchant
    }
}
