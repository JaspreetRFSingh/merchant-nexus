package com.merchant.nexus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway - Central entry point for all client requests.
 * 
 * Demonstrates:
 * - Spring Cloud Gateway for routing
 * - Rate limiting with Redis
 * - Circuit breaker pattern
 * - Request/Response filtering
 * - Centralized cross-cutting concerns
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
