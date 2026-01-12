# Dockerfile for Catalog Service

FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY common-lib/pom.xml common-lib/
COPY catalog-service/pom.xml catalog-service/

RUN cd common-lib && mvn dependency:go-offline -B
RUN cd catalog-service && mvn dependency:go-offline -B

COPY common-lib/src common-lib/src
COPY catalog-service/src catalog-service/src

RUN cd common-lib && mvn clean install -DskipTests -B
RUN cd catalog-service && mvn clean package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /app/catalog-service/target/*.jar app.jar

RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8082/actuator/health || exit 1

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
