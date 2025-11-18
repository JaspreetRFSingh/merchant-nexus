package com.merchant.nexus.service;

import com.merchant.nexus.event.DomainEvent;
import com.merchant.nexus.event.ProductEvents;
import com.merchant.nexus.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka event publisher for product domain events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CatalogEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String PRODUCT_TOPIC = "product-events";

    public void publishProductCreated(Product product) {
        ProductEvents.ProductCreated eventData = ProductEvents.ProductCreated.builder()
                .productId(product.getId())
                .merchantId(product.getMerchantId())
                .name(product.getName())
                .category(product.getCategory())
                .price(product.getPrice() != null ? product.getPrice().getAmount() : null)
                .stockQuantity(product.getStockQuantity())
                .createdAt(product.getCreatedAt())
                .build();

        DomainEvent<ProductEvents.ProductCreated> event = DomainEvent.of(
                "PRODUCT_CREATED",
                product.getId(),
                "Product",
                eventData
        );

        publishEvent(event);
    }

    public void publishProductUpdated(Product product) {
        ProductEvents.ProductUpdated eventData = ProductEvents.ProductUpdated.builder()
                .productId(product.getId())
                .merchantId(product.getMerchantId())
                .updatedFields("name,description,category,price,stock")
                .updatedAt(product.getUpdatedAt())
                .build();

        DomainEvent<ProductEvents.ProductUpdated> event = DomainEvent.of(
                "PRODUCT_UPDATED",
                product.getId(),
                "Product",
                eventData
        );

        publishEvent(event);
    }

    public void publishProductActivated(Product product) {
        ProductEvents.ProductActivated eventData = ProductEvents.ProductActivated.builder()
                .productId(product.getId())
                .merchantId(product.getMerchantId())
                .activatedAt(product.getUpdatedAt())
                .build();

        DomainEvent<ProductEvents.ProductActivated> event = DomainEvent.of(
                "PRODUCT_ACTIVATED",
                product.getId(),
                "Product",
                eventData
        );

        publishEvent(event);
    }

    public void publishStockUpdated(Product product, int previousStock, int newStock, String reason) {
        ProductEvents.StockUpdated eventData = ProductEvents.StockUpdated.builder()
                .productId(product.getId())
                .merchantId(product.getMerchantId())
                .previousStock(previousStock)
                .newStock(newStock)
                .reason(reason)
                .updatedAt(product.getUpdatedAt())
                .build();

        DomainEvent<ProductEvents.StockUpdated> event = DomainEvent.of(
                "STOCK_UPDATED",
                product.getId(),
                "Product",
                eventData
        );

        publishEvent(event);
    }

    public void publishProductDeleted(Product product, String reason) {
        ProductEvents.ProductDeleted eventData = ProductEvents.ProductDeleted.builder()
                .productId(product.getId())
                .merchantId(product.getMerchantId())
                .reason(reason)
                .deletedAt(java.time.Instant.now())
                .build();

        DomainEvent<ProductEvents.ProductDeleted> event = DomainEvent.of(
                "PRODUCT_DELETED",
                product.getId(),
                "Product",
                eventData
        );

        publishEvent(event);
    }

    private void publishEvent(DomainEvent<?> event) {
        CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(PRODUCT_TOPIC, event.getAggregateId(), event);

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
