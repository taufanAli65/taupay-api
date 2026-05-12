package com.example.demo.controllers;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.demo.services.TransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/transactions")
@RequiredArgsConstructor
@Tag(name = "Admin Transactions", description = "Super admin endpoints for transaction monitoring.")
@SecurityRequirement(name = "bearerAuth")
public class AdminTransactionController {
    private final TransactionService transactionService;

    @GetMapping(value = "/{trxId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Subscribe to transaction events", description = "Opens an SSE channel for transaction payment events.")
    public SseEmitter streamTransactionEvents(
            @PathVariable("trxId") String trxId
    ) {
        return transactionService.subscribeToTransactionEvents(trxId);
    }
}
