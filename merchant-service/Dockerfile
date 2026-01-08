# Dockerfile for Merchant Service
# Demonstrates: Multi-stage builds, containerization best practices

FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy parent POM and common-lib first for better layer caching
COPY pom.xml .
COPY common-lib/pom.xml common-lib/
COPY merchant-service/pom.xml merchant-service/

# Download dependencies
RUN cd common-lib && mvn dependency:go-offline -B
RUN cd merchant-service && mvn dependency:go-offline -B

# Copy source code
COPY common-lib/src common-lib/src
COPY merchant-service/src merchant-service/src

# Build the application
RUN cd common-lib && mvn clean install -DskipTests -B
RUN cd merchant-service && mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy built artifact
COPY --from=builder /app/merchant-service/target/*.jar app.jar

# Change ownership
RUN chown -R appuser:appgroup /app

USER appuser

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8081/actuator/health || exit 1

# JVM options for production
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
