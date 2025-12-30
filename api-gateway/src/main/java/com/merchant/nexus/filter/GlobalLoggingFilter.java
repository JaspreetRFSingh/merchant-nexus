package com.merchant.nexus.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.UUID;

/**
 * Global filter for request logging and distributed tracing.
 * Demonstrates: Request tracing, correlation IDs, logging best practices
 */
@Component
@Slf4j
public class GlobalLoggingFilter implements GlobalFilter, Ordered {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String START_TIME_ATTR = "startTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER);
        
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        long startTime = System.currentTimeMillis();
        exchange.getAttributes().put(START_TIME_ATTR, startTime);
        exchange.getAttributes().put(REQUEST_ID_HEADER, requestId);

        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(exchange.getRequest().mutate()
                        .header(REQUEST_ID_HEADER, requestId)
                        .build())
                .response(exchange.getResponse().mutate()
                        .header(REQUEST_ID_HEADER, requestId)
                        .build())
                .build();

        log.info("Request started: {} {} [Request-ID: {}]", 
                exchange.getRequest().getMethod(),
                exchange.getRequest().getPath(),
                requestId);

        return chain.filter(modifiedExchange)
                .doOnSuccess(aVoid -> {
                    long duration = System.currentTimeMillis() - startTime;
                    log.info("Request completed: {} {} Status: {} Duration: {}ms [Request-ID: {}]",
                            exchange.getRequest().getMethod(),
                            exchange.getRequest().getPath(),
                            exchange.getResponse().getStatusCode(),
                            duration,
                            requestId);
                })
                .doOnError(throwable -> {
                    long duration = System.currentTimeMillis() - startTime;
                    log.error("Request failed: {} {} Duration: {}ms [Request-ID: {}] Error: {}",
                            exchange.getRequest().getMethod(),
                            exchange.getRequest().getPath(),
                            duration,
                            requestId,
                            throwable.getMessage());
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
