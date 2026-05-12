package com.example.demo.utils;

import java.math.BigDecimal;
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

    public static long toLongAmount(BigDecimal value) {
        if (value == null) {
            throw new BadRequestException("Amount is required");
        }
        try {
            return value.longValueExact();
        } catch (ArithmeticException ex) {
            throw new BadRequestException("Amount must be a whole number");
        }
    }
}
