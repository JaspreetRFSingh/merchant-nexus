package com.merchant.nexus.service;

import com.merchant.nexus.model.Merchant;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

/**
 * Redis cache service for merchants.
 * Demonstrates: Caching patterns, Redis integration, performance optimization
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String MERCHANT_KEY_PREFIX = "merchant:";
    private static final long CACHE_TTL_HOURS = 24;

    public void cacheMerchant(Merchant merchant) {
        try {
            String key = MERCHANT_KEY_PREFIX + merchant.getId();
            String json = objectMapper.writeValueAsString(merchant);
            redisTemplate.opsForValue().set(key, json, CACHE_TTL_HOURS, TimeUnit.HOURS);
            log.debug("Cached merchant with id: {}", merchant.getId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize merchant for caching", e);
        }
    }

    public Merchant getCachedMerchant(String id) {
        try {
            String key = MERCHANT_KEY_PREFIX + id;
            String json = redisTemplate.opsForValue().get(key);
            if (json != null) {
                log.debug("Cache hit for merchant id: {}", id);
                return objectMapper.readValue(json, Merchant.class);
            }
            log.debug("Cache miss for merchant id: {}", id);
            return null;
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize merchant from cache", e);
            return null;
        }
    }

    public void evictMerchantCache(String id) {
        String key = MERCHANT_KEY_PREFIX + id;
        redisTemplate.delete(key);
        log.debug("Evicted merchant cache for id: {}", id);
    }

    public void updateCachedMerchant(Merchant merchant) {
        cacheMerchant(merchant);
        log.debug("Updated cached merchant with id: {}", merchant.getId());
    }
}
