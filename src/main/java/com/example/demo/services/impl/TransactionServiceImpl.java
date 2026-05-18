package com.example.demo.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.demo.dtos.requests.ReqPaymentCallbackDto;
import com.example.demo.dtos.requests.ReqTransactionDto;
import com.example.demo.dtos.responses.ResTransactionDto;
import com.example.demo.dtos.responses.ResTransactionHistoryDto;
import com.example.demo.entities.AccountEntity;
import com.example.demo.entities.AccountProductTransactionEntity;
import com.example.demo.entities.AccountTransactionEntity;
import com.example.demo.entities.MerchantEntity;
import com.example.demo.entities.OwnerTypeEnum;
import com.example.demo.entities.ProductEntity;
import com.example.demo.entities.ProductTransactionEntity;
import com.example.demo.entities.UserEntity;
import com.example.demo.exceptions.AccountLockedException;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.mappers.TransactionMapper;
import com.example.demo.repositories.AccountProductTransactionRepository;
import com.example.demo.repositories.AccountTransactionRepository;
import com.example.demo.repositories.MerchantRepository;
import com.example.demo.repositories.ProductQuantityRepository;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.repositories.ProductTransactionHistoryRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.repositories.WalletRepository;
import com.example.demo.services.EventStreamService;
import com.example.demo.services.PinAttemptService;
import com.example.demo.services.TransactionCacheService;
import com.example.demo.services.TransactionService;
import com.example.demo.services.UserService;
import com.example.demo.utils.TransactionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {
    private final AccountTransactionRepository accountTransactionRepository;
    private final AccountProductTransactionRepository accountProductTransactionRepository;
    private final ProductTransactionHistoryRepository productTransactionHistoryRepository;
    private final ProductRepository productRepository;
    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionMapper transactionMapper;
    private final TransactionCacheService transactionCacheService;
    private final EventStreamService eventStreamService;
    private final ProductQuantityRepository productQuantityRepository;
    private final PasswordEncoder passwordEncoder;
    private final PinAttemptService pinAttemptService;
    private final UserService userService;

    @Value("${app.transaction.ttl:5m}")
    private java.time.Duration transactionTtl;

    @Value("${app.payment-lock.duration:15m}")
    private java.time.Duration paymentLockDuration;

    @Override
    public ResTransactionDto createTransaction(ReqTransactionDto request, UUID merchant_id) {
        if (merchant_id == null) {
            throw new BadRequestException("Merchant ID is required");
        }

        if (request == null || request.getProducts() == null || request.getProducts().isEmpty()) {
            throw new BadRequestException("Products are required");
        }

        MerchantEntity merchant = merchantRepository.findById(merchant_id)
                .orElseThrow(() -> new DataNotFoundException("Merchant not found"));

        List<ResTransactionDto.ProductItem> productItems = new ArrayList<>();
        Long total = 0L;
        for (ReqTransactionDto.ProductItem item : request.getProducts()) {
            UUID productId = TransactionUtils.parseUuid(item.getProductId(), "product_id");
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BadRequestException("Quantity must be greater than 0");
            }

            ProductEntity product = productRepository.findByIdAndMerchantIdAndIsActiveTrue(productId, merchant_id)
                    .orElseThrow(() -> new DataNotFoundException("Product not found for merchant"));

            productItems.add(transactionMapper.toProductItem(product, item.getQuantity()));

            try {
                long itemTotal = Math.multiplyExact(product.getPrice(), item.getQuantity());
                total = Math.addExact(total, itemTotal);
            } catch (ArithmeticException ex) {
                throw new BadRequestException("Transaction total is too large");
            }
        }

        String trxId = generateTransactionId();
        LocalDateTime createdAt = LocalDateTime.now();

        ResTransactionDto payload = transactionMapper.toResponse(trxId, merchant, createdAt, productItems, total);
        transactionCacheService.save(trxId, payload);

        // Schedule an "expired" event to be sent when the TTL is reached
        CompletableFuture.runAsync(() -> {
            notifySseExpired(trxId);
        }, CompletableFuture.delayedExecutor(transactionTtl.toMillis(), TimeUnit.MILLISECONDS));

        return payload;
    }

    @Override
    public SseEmitter subscribeToTransactionEvents(String trxId) {
        if (trxId == null || trxId.isBlank()) {
            throw new BadRequestException("trx_id is required");
        }

        // Add 5 seconds to the TTL so the SSE connection doesn't timeout before receiving the expired event
        return eventStreamService.subscribe(trxId, transactionTtl.toMillis() + 5000L);
    }

    @Override
    @Transactional
    public void handlePaymentCallback(String trxId, UUID userId, ReqPaymentCallbackDto request) {
        if (userId == null) {
            throw new BadRequestException("User ID is required");
        }

        if (trxId == null || trxId.isBlank()) {
            throw new BadRequestException("trx_id is required");
        }

        ResTransactionDto payload = transactionCacheService.get(trxId);

        UUID merchantId = TransactionUtils.parseUuid(payload.getMerchantId(), "merchant_id");

        MerchantEntity merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new DataNotFoundException("Merchant not found"));
        UserEntity payer = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));
        AccountEntity payerAccount = payer.getAccount();
        if (payerAccount == null) {
            throw new DataNotFoundException("Account for user not found");
        }
        if (payerAccount.getPin() == null || payerAccount.getPin().isBlank()) {
            throw new BadRequestException("PIN is not set for this account");
        }
        if (!passwordEncoder.matches(request.getPin(), payerAccount.getPin())) {
            long attempts = pinAttemptService.recordFailedAttempt(userId, trxId);
            if (attempts >= 3) {
                userService.lockPayments(userId, LocalDateTime.now().plus(paymentLockDuration));
                pinAttemptService.clearAttempts(userId, trxId);
                throw new AccountLockedException("Too many incorrect PIN attempts. User account is locked for payments temporarily.");
            }
            throw new BadRequestException("Invalid PIN");
        }
        pinAttemptService.clearAttempts(userId, trxId);

        Long amount = payload.getTotal();

        int payerWalletUpdated = walletRepository.decrementAmountIfEnough(userId, OwnerTypeEnum.USER, amount);
        if (payerWalletUpdated == 0) {
            if (!walletRepository.existsByOwnerIdAndOwnerType(userId, OwnerTypeEnum.USER)) {
                throw new DataNotFoundException("User wallet not found");
            }
            throw new BadRequestException("Insufficient wallet balance");
        }

        int merchantWalletUpdated = walletRepository.incrementAmount(merchantId, OwnerTypeEnum.MERCHANT, amount);
        if (merchantWalletUpdated == 0) {
            throw new DataNotFoundException("Merchant wallet not found");
        }

        Map<UUID, Integer> requestedQuantitiesByProductId = new LinkedHashMap<>();
        for (ResTransactionDto.ProductItem item : payload.getProducts()) {
            UUID productId = TransactionUtils.parseUuid(item.getProductId(), "product_id");
            requestedQuantitiesByProductId.merge(productId, item.getQuantity(), Integer::sum);
        }

        for (Map.Entry<UUID, Integer> entry : requestedQuantitiesByProductId.entrySet()) {
            int stockUpdated = productQuantityRepository.decrementStockIfEnough(entry.getKey(), entry.getValue());
            if (stockUpdated == 0) {
                if (!productQuantityRepository.existsByProductId(entry.getKey())) {
                    throw new DataNotFoundException("Product quantity not found");
                }
                throw new BadRequestException("Insufficient product stock");
            }
        }

        Map<UUID, ProductEntity> productsById = new java.util.HashMap<>();
        for (ProductEntity product : productRepository.findAllById(requestedQuantitiesByProductId.keySet())) {
            productsById.put(product.getId(), product);
        }
        if (productsById.size() != requestedQuantitiesByProductId.size()) {
            throw new DataNotFoundException("Product not found");
        }

        String merchantCategory = merchant.getCategory() != null ? merchant.getCategory().getName() : "UNSPECIFIED";
        AccountTransactionEntity accountTransaction = transactionMapper.toAccountTransaction(
            merchant,
            payer,
            amount,
            merchantCategory
        );
        AccountTransactionEntity savedAccountTransaction = accountTransactionRepository.save(accountTransaction);

        List<ProductTransactionEntity> productTransactions = new ArrayList<>();
        for (ResTransactionDto.ProductItem item : payload.getProducts()) {
            UUID productId = TransactionUtils.parseUuid(item.getProductId(), "product_id");
            ProductEntity product = productsById.get(productId);

                Long productPrice = item.getPrice();
                ProductTransactionEntity productTransaction = transactionMapper.toProductTransaction(
                    product,
                    item,
                    productPrice
                );
            productTransactions.add(productTransaction);
        }

        List<ProductTransactionEntity> savedProductTransactions = productTransactionHistoryRepository.saveAll(productTransactions);
        List<AccountProductTransactionEntity> links = new ArrayList<>();
        for (ProductTransactionEntity productTransaction : savedProductTransactions) {
            links.add(transactionMapper.toAccountProductTransaction(savedAccountTransaction, productTransaction));
        }
        accountProductTransactionRepository.saveAll(links);

        notifySse(trxId, "PAID", payload.getTotal());
        transactionCacheService.evict(trxId);
    }

    @Override
    public Page<ResTransactionHistoryDto> getTransactionHistory(
            UUID profileId, LocalDate startDate, LocalDate endDate, int page, int size, boolean isMerchant
    ) {
        
        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : LocalDateTime.now().minusDays(30);
        LocalDateTime end = (endDate != null) ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        if (start.isAfter(end)) {
            throw new BadRequestException("startDate must be before or equal to endDate");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AccountTransactionEntity> transactionPage;

        if (isMerchant) {
            transactionPage = accountTransactionRepository.findAllByReceiverIdAndCreatedAtBetween(profileId, start, end, pageable);
        } else {
            transactionPage = accountTransactionRepository.findAllByRequesterIdAndCreatedAtBetween(profileId, start, end, pageable);
        }

        return transactionPage.map(entity -> transactionMapper.toHistoryResponse(entity, isMerchant));
    }

    private void notifySse(String trxId, String status, Long total) {
        Map<String, Object> payload = Map.of(
                "trx_id", trxId,
                "status", status,
                "total", total
        );
        eventStreamService.publish(trxId, "payment", payload);
    }

    private void notifySseExpired(String trxId) {
        Map<String, Object> payload = Map.of(
                "trx_id", trxId,
                "status", "EXPIRED",
                "message", "Transaction has expired due to timeout"
        );
        eventStreamService.publish(trxId, "expired", payload);
    }

    private String generateTransactionId() {
        return "TRX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
