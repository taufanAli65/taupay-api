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
    private static final String TRX_MERCHANT_INDEX_PREFIX = "trx:merchant:";
    private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.transaction.ttl:5m}")
    private Duration transactionTtl;

    @Value("${app.idempotency.ttl:15m}")
    private Duration idempotencyTtl;

    @Override
    public void save(String trxId, ResTransactionDto payload) {
        String cacheKey = buildCacheKey(trxId);
        try {
            String json = objectMapper.writeValueAsString(payload);
            stringRedisTemplate.opsForValue().set(cacheKey, json, transactionTtl);
            // Register the cache key in the merchant index ZSET using the key's expiration timestamp as score.
            // Also prune expired members to avoid historical accumulation.
            if (payload != null && payload.getMerchant() != null && payload.getMerchant().getMerchantId() != null) {
                String merchantIndexKey = buildMerchantIndexKey(payload.getMerchant().getMerchantId());
                long now = System.currentTimeMillis();
                long expireAt = now + transactionTtl.toMillis();
                stringRedisTemplate.opsForZSet().removeRangeByScore(merchantIndexKey, 0, now);
                stringRedisTemplate.opsForZSet().add(merchantIndexKey, cacheKey, (double) expireAt);
                stringRedisTemplate.expire(merchantIndexKey, transactionTtl);
            }
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
    public boolean isAlreadyProcessed(String trxId, UUID userId) {
        String key = buildIdempotencyKey(trxId, userId);
        Boolean exists = stringRedisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public void markAsProcessed(String trxId, UUID userId) {
        String key = buildIdempotencyKey(trxId, userId);
        stringRedisTemplate.opsForValue().set(key, "1", idempotencyTtl);
    }

    @Override
    public void evict(String trxId, UUID merchantId) {
        String cacheKey = buildCacheKey(trxId);
        stringRedisTemplate.delete(cacheKey);
        if (merchantId != null) {
            String merchantIndexKey = buildMerchantIndexKey(merchantId.toString());
            stringRedisTemplate.opsForZSet().remove(merchantIndexKey, cacheKey);
            Long remaining = stringRedisTemplate.opsForZSet().zCard(merchantIndexKey);
            if (remaining == null || remaining == 0) {
                stringRedisTemplate.delete(merchantIndexKey);
            }
        }
        log.info("Evicted transaction cache: trxId={}, cacheKey={}, merchantId={}", trxId, cacheKey, merchantId);
    }

    @Override
    public void evictByMerchantId(UUID merchantId) {
        if (merchantId == null) {
            return;
        }
        String merchantIndexKey = buildMerchantIndexKey(merchantId.toString());
        long now = System.currentTimeMillis();
        // Prune expired members first (score <= now)
        stringRedisTemplate.opsForZSet().removeRangeByScore(merchantIndexKey, 0, now);

        // Fetch currently live members (score > now)
        Set<String> members = stringRedisTemplate.opsForZSet().rangeByScore(merchantIndexKey, (double) (now + 1), Double.POSITIVE_INFINITY);
        if (members == null || members.isEmpty()) {
            // nothing live to evict; delete the (now-empty) index key
            stringRedisTemplate.delete(merchantIndexKey);
            log.info("No live transaction cache entries to evict for merchantId={}", merchantId);
            return;
        }

        // Batch delete live transaction keys
        stringRedisTemplate.delete(members);
        // Remove these members from the index and delete the index key to fully clear state for deactivated merchant
        stringRedisTemplate.opsForZSet().remove(merchantIndexKey, members.toArray());
        stringRedisTemplate.delete(merchantIndexKey);
        log.info("Evicted {} live transaction cache entries for merchantId={}", members.size(), merchantId);
    }

    private String buildCacheKey(String trxId) {
        return TRX_KEY_PREFIX + normalizeTrxId(trxId);
    }

    private String buildMerchantIndexKey(String merchantId) {
        return TRX_MERCHANT_INDEX_PREFIX + merchantId;
    }

    private String buildIdempotencyKey(String trxId, UUID userId) {
        return IDEMPOTENCY_KEY_PREFIX + normalizeTrxId(trxId) + ":" + userId;
    }

    private String normalizeTrxId(String trxId) {
        if (trxId == null) {
            return null;
        }
        return trxId.trim();
    }
}
