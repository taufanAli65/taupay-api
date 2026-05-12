package com.example.demo.services.impl;

import java.time.Duration;

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

@Service
@RequiredArgsConstructor
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
            throw new DataNotFoundException("Transaction not found or expired");
        }
        try {
            return objectMapper.readValue(json, ResTransactionDto.class);
        } catch (JsonProcessingException ex) {
            throw new BadRequestException("Failed to read transaction payload");
        }
    }

    @Override
    public void evict(String trxId) {
        stringRedisTemplate.delete(buildCacheKey(trxId));
    }

    private String buildCacheKey(String trxId) {
        return TRX_KEY_PREFIX + trxId;
    }
}
