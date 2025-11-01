package com.merchant.nexus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Catalog Service - Microservice for product catalog management with Elasticsearch.
 * 
 * Demonstrates:
 * - Elasticsearch integration for full-text search
 * - Event-driven catalog updates via Kafka
 * - Redis caching for high-performance reads
 * - Complex search queries with filters
 */
@SpringBootApplication
public class CatalogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogServiceApplication.class, args);
    }
}
