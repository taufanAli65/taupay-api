package com.example.demo.services;

import com.example.demo.dtos.responses.ResTransactionDto;

import java.util.UUID;

public interface TransactionCacheService {
    void save(String trxId, ResTransactionDto payload);

    ResTransactionDto get(String trxId);

    boolean isAlreadyProcessed(String trxId, UUID userId);

    void markAsProcessed(String trxId, UUID userId);

    void evict(String trxId, UUID merchantId);

    void evictByMerchantId(UUID merchantId);
}
