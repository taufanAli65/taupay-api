package com.example.demo.services;

import com.example.demo.dtos.responses.ResTransactionDto;

import java.util.UUID;

public interface TransactionCacheService {
    void save(String trxId, ResTransactionDto payload);

    ResTransactionDto get(String trxId);

    void evict(String trxId);

    void evictByMerchantId(UUID merchantId);
}
