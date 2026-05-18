package com.example.demo.services;

import java.util.UUID;

public interface PinAttemptService {
    long recordFailedAttempt(UUID userId, String trxId);

    void clearAttempts(UUID userId, String trxId);
}
