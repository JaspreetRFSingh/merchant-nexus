package com.merchant.nexus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Settlement Service - Microservice for merchant settlements and payouts.
 * 
 * Demonstrates:
 * - Financial data consistency patterns
 * - Saga pattern for distributed transactions
 * - Circuit breaker pattern with Resilience4j
 * - Distributed locking with Redis
 * - Audit trails for financial operations
 */
@SpringBootApplication
@EnableJpaAuditing
public class SettlementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SettlementServiceApplication.class, args);
    }
}
