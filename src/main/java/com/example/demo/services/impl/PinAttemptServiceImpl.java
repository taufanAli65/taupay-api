package com.example.demo.services.impl;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.services.PinAttemptService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PinAttemptServiceImpl implements PinAttemptService {
    private static final String PIN_ATTEMPT_KEY_PREFIX = "pin-attempt:";

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${app.transaction.ttl:5m}")
    private Duration transactionTtl;

    @Override
    public long recordFailedAttempt(UUID userId, String trxId) {
        String key = buildKey(userId, trxId);
        Long attempts = stringRedisTemplate.opsForValue().increment(key);
        stringRedisTemplate.expire(key, transactionTtl);
        return attempts != null ? attempts : 0L;
    }

    @Override
    public void clearAttempts(UUID userId, String trxId) {
        stringRedisTemplate.delete(buildKey(userId, trxId));
    }

    private String buildKey(UUID userId, String trxId) {
        return PIN_ATTEMPT_KEY_PREFIX + userId + ":" + trxId.trim();
    }
}
