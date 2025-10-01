package com.merchant.nexus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Merchant Service - Core microservice for merchant management.
 * 
 * Demonstrates:
 * - Spring Boot 3.x with Java 17
 * - Microservices architecture
 * - JPA/Hibernate with MySQL
 * - Event-driven architecture with Kafka
 * - Redis caching
 * - RESTful API design
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableFeignClients
public class MerchantServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MerchantServiceApplication.class, args);
    }
}
