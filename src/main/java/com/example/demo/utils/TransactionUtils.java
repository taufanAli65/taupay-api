package com.example.demo.utils;

import java.util.UUID;

import com.example.demo.exceptions.BadRequestException;

public final class TransactionUtils {
    private TransactionUtils() {
    }

    public static UUID parseUuid(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(fieldName + " is required");
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(fieldName + " is invalid");
        }
    }
}
