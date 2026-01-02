package com.merchant.nexus.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Fallback controller for circuit breaker pattern.
 * Demonstrates: Graceful degradation, error handling
 */
@RestController
@Slf4j
public class FallbackController {

    public Mono<ResponseEntity<Map<String, Object>>> merchantFallback() {
        return Mono.just(createErrorResponse("Merchant Service is temporarily unavailable"));
    }

    public Mono<ResponseEntity<Map<String, Object>>> catalogFallback() {
        return Mono.just(createErrorResponse("Catalog Service is temporarily unavailable"));
    }

    public Mono<ResponseEntity<Map<String, Object>>> settlementFallback() {
        return Mono.just(createErrorResponse("Settlement Service is temporarily unavailable"));
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("errorCode", "SERVICE_UNAVAILABLE");
        response.put("message", message);
        response.put("timestamp", Instant.now().toString());
        response.put("suggestion", "Please try again later or contact support");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
