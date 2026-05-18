package com.example.demo.services;

import java.time.LocalDate;
import com.example.demo.dtos.requests.ReqPaymentCallbackDto;
import com.example.demo.dtos.requests.ReqTransactionDto;
import com.example.demo.dtos.requests.ReqPaginationDto;
import com.example.demo.dtos.responses.ResTransactionDto;
import com.example.demo.dtos.responses.ResTransactionHistoryDto;
import org.springframework.data.domain.Page;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.UUID;

public interface TransactionService {
    ResTransactionDto createTransaction(ReqTransactionDto request, UUID merchant_id);

    ResTransactionDto getTransactionDetails(String trxId);

    SseEmitter subscribeToTransactionEvents(String trxId);

    void handlePaymentCallback(String trxId, UUID userId, ReqPaymentCallbackDto request);

    Page<ResTransactionHistoryDto> getTransactionHistory(UUID profileId, LocalDate startDate, LocalDate endDate, ReqPaginationDto paginationDto, boolean isMerchant);
}
