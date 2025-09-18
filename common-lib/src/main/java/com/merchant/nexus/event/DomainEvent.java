package com.merchant.nexus.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Base event class for domain events.
 * Demonstrates: Event sourcing patterns, event metadata, correlation IDs
 */
@Data
@Builder
public class DomainEvent<T> {
    private String eventId;
    private String eventType;
    private String aggregateId;
    private String aggregateType;
    private T data;
    private Map<String, String> metadata;
    @JsonProperty("occurredAt")
    private Instant occurredAt;
    private String correlationId;
    private String causationId;

    public static <T> DomainEvent<T> of(String eventType, String aggregateId, 
                                         String aggregateType, T data) {
        return DomainEvent.<T>builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .aggregateId(aggregateId)
                .aggregateType(aggregateType)
                .data(data)
                .occurredAt(Instant.now())
                .correlationId(UUID.randomUUID().toString())
                .build();
    }

    public static <T> DomainEvent<T> of(String eventType, String aggregateId, 
                                         String aggregateType, T data, String correlationId) {
        return DomainEvent.<T>builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .aggregateId(aggregateId)
                .aggregateType(aggregateType)
                .data(data)
                .occurredAt(Instant.now())
                .correlationId(correlationId)
                .build();
    }
}
