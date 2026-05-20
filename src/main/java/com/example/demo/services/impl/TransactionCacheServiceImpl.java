package com.example.demo.services.impl;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.dtos.responses.ResTransactionDto;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.services.TransactionCacheService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionCacheServiceImpl implements TransactionCacheService {
    private static final String TRX_KEY_PREFIX = "trx:";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.transaction.ttl:5m}")
    private Duration transactionTtl;

    @Override
    public void save(String trxId, ResTransactionDto payload) {
        String cacheKey = buildCacheKey(trxId);
        try {
            String json = objectMapper.writeValueAsString(payload);
            stringRedisTemplate.opsForValue().set(cacheKey, json, transactionTtl);
        } catch (JsonProcessingException ex) {
            throw new BadRequestException("Failed to cache transaction payload");
        }
    }

    @Override
    public ResTransactionDto get(String trxId) {
        String cacheKey = buildCacheKey(trxId);
        String json = stringRedisTemplate.opsForValue().get(cacheKey);
        if (json == null || json.isBlank()) {
            log.warn("[CACHE MISS] Transaction not found or expired: trxId={}, cacheKey={}", trxId, cacheKey);
            throw new DataNotFoundException("Transaction not found or expired");
        }
        try {
            ResTransactionDto payload = objectMapper.readValue(json, ResTransactionDto.class);
            log.info("[CACHE HIT] Transaction data retrieved from Redis: trxId={}", trxId);
            return payload;
        } catch (JsonProcessingException ex) {
            throw new BadRequestException("Failed to read transaction payload");
        }
    }

    @Override
    public void evict(String trxId) {
        String cacheKey = buildCacheKey(trxId);
        stringRedisTemplate.delete(cacheKey);
        log.info("Evicted transaction cache: trxId={}, cacheKey={}", trxId, cacheKey);
    }

    @Override
    public void evictByMerchantId(UUID merchantId) {
        if (merchantId == null) {
            return;
        }

        Set<String> keys = stringRedisTemplate.keys(TRX_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return;
        }

        String merchantIdValue = merchantId.toString();
        for (String key : keys) {
            String json = stringRedisTemplate.opsForValue().get(key);
            if (json == null || json.isBlank()) {
                continue;
            }
            try {
                ResTransactionDto payload = objectMapper.readValue(json, ResTransactionDto.class);
                ResTransactionDto.MerchantSummary merchant = payload.getMerchant();
                if (merchant != null && merchantIdValue.equals(merchant.getMerchantId())) {
                    stringRedisTemplate.delete(key);
                    log.info("Evicted transaction cache for merchant: merchantId={}, cacheKey={}", merchantIdValue, key);
                }
            } catch (JsonProcessingException ex) {
                log.warn("Failed to parse transaction cache payload for key: {}", key, ex);
            }
        }
    }

    private String buildCacheKey(String trxId) {
        return TRX_KEY_PREFIX + normalizeTrxId(trxId);
    }

    private String normalizeTrxId(String trxId) {
        if (trxId == null) {
            return null;
        }
        return trxId.trim();
    }
}
