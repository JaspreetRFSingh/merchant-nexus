# Merchant Nexus

A production-ready microservices-based **Merchant Catalog & Settlement System** designed to demonstrate enterprise-grade software engineering skills for e-commerce marketplace platforms.

## 🎯 Project Overview

This system handles merchant onboarding, product catalog management, and settlement processing for e-commerce platforms:
- **Merchant Management** - Onboarding, verification, and lifecycle management
- **Product Catalog** - Full-text search, inventory management with Elasticsearch
- **Settlement Processing** - Financial payouts with data consistency guarantees

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                            API Gateway (8080)                            │
│         Rate Limiting │ Circuit Breaker │ Request Tracing               │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
        ┌───────────────────────────┼───────────────────────────┐
        ▼                           ▼                           ▼
┌───────────────┐          ┌───────────────┐          ┌───────────────┐
│   Merchant    │          │    Catalog    │          │  Settlement   │
│   Service     │          │    Service    │          │    Service    │
│   (8081)      │          │    (8082)     │          │    (8083)     │
│               │          │               │          │               │
│ • Spring Boot │          │ • Elasticsearch│         │ • Saga Pattern│
│ • JPA/MySQL   │          │ • Full-text   │          │ • Distributed │
│ • Redis Cache │          │   Search      │          │   Locking     │
│ • Kafka Events│          │ • Kafka Consumer│        │ • Circuit     │
└───────┬───────┘          └───────┬───────┘          │   Breaker     │
        │                          │                   └───────┬───────┘
        │                          │                           │
        └──────────────────────────┼───────────────────────────┘
                                   │
        ┌──────────────────────────┼───────────────────────────┐
        ▼                          ▼                           ▼
┌───────────────┐         ┌───────────────┐         ┌───────────────┐
│     MySQL     │         │  Elasticsearch │         │     Redis     │
│   (3306,3307) │         │     (9200)     │         │     (6379)    │
└───────────────┘         └───────────────┘         └───────────────┘
                                   │
                          ┌───────────────┐
                          │     Kafka     │
                          │     (9092)    │
                          │  Topics:      │
                          │ • merchant-   │
                          │   events      │
                          │ • product-    │
                          │   events      │
                          │ • settlement- │
                          │   events      │
                          └───────────────┘
```

## 🚀 Tech Stack

| Category | Technology |
|----------|------------|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.2.x, Spring Cloud 2023.x |
| **Database** | MySQL 8.0 (JPA/Hibernate) |
| **Search** | Elasticsearch 8.x |
| **Cache** | Redis 7.x |
| **Message Broker** | Apache Kafka 3.6.x |
| **API Gateway** | Spring Cloud Gateway |
| **Resilience** | Resilience4j (Circuit Breaker, Retry) |
| **Distributed Locking** | Redisson |
| **Containerization** | Docker, Docker Compose |
| **Documentation** | OpenAPI/Swagger |

## 📁 Project Structure

```
merchant-nexus/
├── common-lib/              # Shared models, DTOs, events
│   ├── model/               # Domain models (Merchant, Product, Settlement)
│   ├── dto/                 # Request/Response DTOs
│   ├── event/               # Domain events for Kafka
│   └── exception/           # Custom exceptions
├── merchant-service/        # Merchant management microservice
│   ├── controller/          # REST APIs
│   ├── service/             # Business logic, Kafka publisher
│   ├── repository/          # JPA entities, repositories
│   └── config/              # Kafka, Redis configuration
├── catalog-service/         # Product catalog microservice
│   ├── controller/
│   ├── service/
│   ├── repository/          # Elasticsearch documents
│   └── config/
├── settlement-service/      # Settlement processing microservice
│   ├── controller/
│   ├── service/             # Saga pattern, distributed locking
│   ├── repository/
│   └── config/              # Redisson configuration
├── api-gateway/             # API Gateway
│   ├── filter/              # Global filters (logging, tracing)
│   └── config/              # Route configuration, fallbacks
├── docker-compose.yml       # Infrastructure orchestration
└── README.md
```

## 🔧 Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Git

### Quick Start with Docker

```bash
# Clone the repository
cd merchant-nexus

# Start all infrastructure and services
docker-compose up -d

# Wait for services to be healthy (check with)
docker-compose ps

# Access Swagger UI
open http://localhost:8080/swagger-ui.html
```

### Local Development (Without Docker)

1. **Start Infrastructure:**
```bash
# Start MySQL, Redis, Elasticsearch, Kafka
docker-compose up -d mysql-merchant mysql-settlement redis elasticsearch kafka zookeeper
```

2. **Initialize Databases:**
```bash
# Run schema scripts
mysql -h localhost -P 3306 -u merchant_user -p < merchant-service/src/main/resources/db/schema.sql
mysql -h localhost -P 3307 -u settlement_user -p < settlement-service/src/main/resources/db/schema.sql
```

3. **Build and Run Services:**
```bash
# Build all modules
mvn clean install -DskipTests

# Run individual services
cd merchant-service && mvn spring-boot:run
cd ../catalog-service && mvn spring-boot:run
cd ../settlement-service && mvn spring-boot:run
cd ../api-gateway && mvn spring-boot:run
```

## 📖 API Documentation

### Merchant Service (Port 8081)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/merchants` | Create a new merchant |
| GET | `/api/v1/merchants/{id}` | Get merchant by ID |
| GET | `/api/v1/merchants` | List all merchants (paginated) |
| POST | `/api/v1/merchants/{id}/verify` | Verify a merchant |
| POST | `/api/v1/merchants/{id}/suspend` | Suspend a merchant |
| POST | `/api/v1/merchants/{id}/activate` | Activate a suspended merchant |

**Example: Create Merchant**
```bash
curl -X POST http://localhost:8081/api/v1/merchants \
  -H "Content-Type: application/json" \
  -d '{
    "businessName": "Seoul Kimchi House",
    "businessRegistrationNumber": "123-45-67890",
    "ownerName": "Kim Min-su",
    "email": "kim@seoulkimchi.kr",
    "phone": "+82-10-1234-5678",
    "address": {
      "street": "123 Gangnam-daero",
      "city": "Seoul",
      "postalCode": "06000",
      "country": "South Korea"
    }
  }'
```

### Catalog Service (Port 8082)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/catalog/merchants/{merchantId}/products` | Create product |
| GET | `/api/v1/catalog/products/{id}` | Get product by ID |
| GET | `/api/v1/catalog/products/search` | Search products |
| PUT | `/api/v1/catalog/products/{id}` | Update product |
| PUT | `/api/v1/catalog/products/{id}/stock` | Update stock |

**Example: Search Products**
```bash
curl "http://localhost:8082/api/v1/catalog/products/search?keyword=kimchi&minPrice=10000&maxPrice=50000&page=0&size=20"
```

### Settlement Service (Port 8083)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/settlements` | Create settlement request |
| GET | `/api/v1/settlements/{id}` | Get settlement details |
| POST | `/api/v1/settlements/{id}/approve` | Approve settlement |
| POST | `/api/v1/settlements/{id}/process` | Process payment |
| POST | `/api/v1/settlements/{id}/adjust` | Apply adjustment |

## 🎯 Key Engineering Concepts Demonstrated

### 1. Microservices Architecture
- Independent deployable services
- Domain-driven design boundaries
- Inter-service communication via events

### 2. Event-Driven Architecture
```java
// Publishing domain events to Kafka
eventPublisher.publishMerchantCreated(merchant);
eventPublisher.publishProductCreated(product);
eventPublisher.publishSettlementCompleted(settlement, paymentRef);
```

### 3. Data Consistency Patterns

**Pessimistic Locking (Settlement Service):**
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT s FROM SettlementEntity s WHERE s.id = :id")
Optional<SettlementEntity> findByIdForUpdate(@Param("id") String id);
```

**Distributed Locking (Redisson):**
```java
RLock lock = redissonClient.getLock("settlement:process:" + settlementId);
boolean locked = lock.tryLock(5, 30, TimeUnit.SECONDS);
```

**Saga Pattern:**
- Settlement processing with compensating transactions
- Event-driven coordination between services

### 4. Resilience Patterns

**Circuit Breaker (Resilience4j):**
```java
@CircuitBreaker(name = "paymentGateway", fallbackMethod = "processPaymentFallback")
@Retry(name = "paymentProcessing")
public SettlementDTO.SettlementResponse processSettlement(String settlementId)
```

**Rate Limiting (API Gateway):**
```yaml
filters:
  - name: RequestRateLimiter
    args:
      redis-rate-limiter.replenishRate: 100
      redis-rate-limiter.burstCapacity: 200
```

### 5. Caching Strategy
```java
@Service
public class MerchantCacheService {
    public void cacheMerchant(Merchant merchant) {
        String key = "merchant:" + merchant.getId();
        String json = objectMapper.writeValueAsString(merchant);
        redisTemplate.opsForValue().set(key, json, 24, TimeUnit.HOURS);
    }
}
```

### 6. Full-Text Search (Elasticsearch)
```java
@Query("{\"bool\": {\"should\": [{\"match\": {\"name\": {\"query\": \"?0\", \"boost\": 2.0}}}, " +
       "{\"match\": {\"description\": \"?0\"}}, {\"term\": {\"tags\": \"?0\"}}]}}")
Page<ProductDocument> searchProducts(String query, Pageable pageable);
```

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run tests with coverage
mvn clean test jacoco:report

# Integration tests
mvn verify -Pintegration
```

## 📊 Monitoring & Observability

### Health Checks
- `/actuator/health` - Service health status
- `/actuator/metrics` - Application metrics

### Kafka UI
Access at `http://localhost:8090` to monitor:
- Topic health
- Message throughput
- Consumer lag

### API Gateway Dashboard
- `/actuator/gateway/routes` - Active routes
- `/actuator/gateway/globalfilters` - Applied filters

## 🔐 Security Considerations

This demo focuses on architecture patterns. For production:
- Add JWT/OAuth2 authentication
- Implement RBAC (Role-Based Access Control)
- Enable HTTPS/TLS
- Add input sanitization
- Configure CORS properly
- Implement API key management

## 📈 Scalability Considerations

1. **Horizontal Scaling**: All services are stateless and can be scaled horizontally
2. **Database Sharding**: Merchant data can be sharded by merchant ID
3. **Elasticsearch Clustering**: Add more nodes for search scalability
4. **Kafka Partitions**: Increase partitions for higher throughput
5. **Redis Cluster**: For distributed caching at scale

## 🎓 Learning Outcomes

This project demonstrates proficiency in:

| Skill Area | Technologies/Patterns |
|------------|----------------------|
| **Backend Development** | Java 17, Spring Boot 3.x, JPA/Hibernate |
| **Database Design** | MySQL schema design, indexing, transactions |
| **Search Engineering** | Elasticsearch mappings, analyzers, queries |
| **Messaging** | Kafka producers/consumers, event sourcing |
| **Caching** | Redis patterns, Redisson distributed locks |
| **API Design** | RESTful APIs, OpenAPI documentation |
| **Resilience** | Circuit breakers, retry, bulkhead patterns |
| **DevOps** | Docker, Docker Compose, multi-stage builds |
| **System Design** | Microservices, CQRS, Saga patterns |

## 🤝 Contributing

This is a portfolio project. Feel free to:
1. Fork the repository
2. Add new features (payment integration, notifications, etc.)
3. Improve test coverage
4. Add monitoring dashboards

## 📄 License

This project is for educational/portfolio purposes.

## 👤 Author

Created as a side project demonstrating skills for Senior Software Engineer positions.

---

**Keywords**: Microservices, Spring Boot, Kafka, Elasticsearch, Redis, MySQL, Docker, API Gateway, Event-Driven Architecture, Distributed Systems, Java 17