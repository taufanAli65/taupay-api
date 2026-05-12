package com.example.demo.services;

import java.util.UUID;

import com.example.demo.dtos.requests.ReqTransactionCallbackDto;
import com.example.demo.dtos.requests.ReqTransactionDto;
import com.example.demo.dtos.responses.ResTransactionDto;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface TransactionService {
    ResTransactionDto createTransaction(ReqTransactionDto request, UUID merchantId);

    SseEmitter subscribeToTransactionEvents(String trxId);

    void handlePaymentCallback(ReqTransactionCallbackDto request);
}
