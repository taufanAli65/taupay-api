package com.example.demo.controllers;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.http.MediaType;

import com.example.demo.dtos.requests.ReqPaymentCallbackDto;
import com.example.demo.dtos.requests.ReqTransactionDto;
import com.example.demo.dtos.responses.BaseResponse;
import com.example.demo.dtos.responses.ResTransactionDto;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.services.TransactionService;
import com.example.demo.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Endpoints for creating transactions and receiving payment callbacks.")
public class TransactionController {
    private final TransactionService transactionService;

    @PreAuthorize("hasRole('MERCHANT')")
    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create transaction", description = "Creates a transaction request for a merchant.")
    public ResponseEntity<BaseResponse<ResTransactionDto>> createTransaction(
            @Valid @RequestBody ReqTransactionDto request
    ) {
        UUID merchantId = SecurityUtils.getCurrentProfileId();
        ResTransactionDto transaction = transactionService.createTransaction(request, merchantId);
        BaseResponse<ResTransactionDto> response = BaseResponse.success("Transaction Created Successfully", transaction);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{trxId}/callback")
    @Operation(summary = "Payment callback", description = "Receives payment gateway callback for a transaction.")
    public ResponseEntity<BaseResponse<Void>> paymentCallback(
            @PathVariable String trxId,
            @Valid @RequestBody ReqPaymentCallbackDto request
    ) {
        if (!trxId.equals(request.getTrxId())) {
            throw new BadRequestException("trx_id in request body must match the URL path");
        }
        UUID userId = SecurityUtils.getCurrentProfileId();
        transactionService.handlePaymentCallback(trxId, userId, request);
        BaseResponse<Void> response = BaseResponse.success("Payment Callback Processed", null);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{trxId}/status", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Subscribe to transaction events", description = "Opens an SSE channel for transaction payment events.")
    public SseEmitter streamTransactionEvents(
            @PathVariable("trxId") String trxId
    ) {
        return transactionService.subscribeToTransactionEvents(trxId);
    }
}
