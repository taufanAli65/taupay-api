package com.example.demo.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.function.Function;

import com.example.demo.dtos.requests.ReqPaginationDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import com.example.demo.exceptions.DuplicateResourceException;
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

        MerchantEntity merchant = merchantRepository.findByIdAndIsActiveTrue(merchant_id)
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
            try {
                // Double-check cache: If it's already gone (PAID and evicted), don't send EXPIRED
                transactionCacheService.get(trxId);
                notifySseExpired(trxId);
            } catch (DataNotFoundException e) {
                log.debug("Transaction {} already paid or handled, skipping expiration broadcast.", trxId);
            }
        }, CompletableFuture.delayedExecutor(transactionTtl.toMillis(), TimeUnit.MILLISECONDS));

        return payload;
    }

    @Override
    public ResTransactionDto getTransactionDetails(String trxId) {
        if (trxId == null || trxId.isBlank()) {
            throw new BadRequestException("trx_id is required");
        }
        return transactionCacheService.get(trxId);
    }

    @Override
    public SseEmitter subscribeToTransactionEvents(String trxId) {
        if (trxId == null || trxId.isBlank()) {
            throw new BadRequestException("trx_id is required");
        }

        // Add 5 seconds to the TTL so the SSE connection doesn't timeout before receiving the expired event
        SseEmitter emitter = eventStreamService.subscribe(trxId, transactionTtl.toMillis() + 5000L);

        // Immediate check: If transaction is already gone from cache (expired or paid), 
        // notify the client immediately so they don't wait in silence.
        try {
            transactionCacheService.get(trxId);
        } catch (DataNotFoundException e) {
            notifySseExpired(trxId);
        }

        return emitter;
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

        if (transactionCacheService.isAlreadyProcessed(trxId, userId)) {
            throw new DuplicateResourceException("Transaction already processed");
        }

        ResTransactionDto payload = transactionCacheService.get(trxId);
        if (payload == null) {
            throw new BadRequestException("Transaction payload not found or expired");
        }
        if (payload.getMerchant() == null || payload.getMerchant().getMerchantId() == null
                || payload.getMerchant().getMerchantId().isBlank()) {
            throw new BadRequestException("merchant_id is missing from transaction payload");
        }

        UUID merchantId = TransactionUtils.parseUuid(payload.getMerchant().getMerchantId(), "merchant_id");

        MerchantEntity merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new DataNotFoundException("Merchant not found"));
        if (merchant.getIsActive() == null || Boolean.FALSE.equals(merchant.getIsActive())) {
            throw new BadRequestException("Merchant is inactive");
        }
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
        
        Map<UUID, Integer> requestedQuantitiesByProductId = payload.getProducts().stream()
                .collect(Collectors.toMap(
                        item -> TransactionUtils.parseUuid(item.getProductId(), "product_id"),
                        ResTransactionDto.ProductItem::getQuantity,
                        Integer::sum
                ));

        // Sort the Product IDs to prevent database deadlocks
        List<UUID> sortedProductIds = requestedQuantitiesByProductId.keySet().stream()
                .sorted()
                .toList();

        for (UUID productId : sortedProductIds) {
            int quantity = requestedQuantitiesByProductId.get(productId);
            int stockUpdated = productQuantityRepository.decrementStockIfEnough(productId, quantity);
            
            if (stockUpdated == 0) {
                if (!productQuantityRepository.existsByProductId(productId)) {
                    throw new DataNotFoundException("Product quantity not found for ID: " + productId);
                }
                throw new BadRequestException("Insufficient product stock for ID: " + productId);
            }
        }

        Map<UUID, ProductEntity> productsById = productRepository.findAllById(sortedProductIds).stream()
                .collect(Collectors.toMap(ProductEntity::getId, Function.identity()));

        if (productsById.size() != requestedQuantitiesByProductId.size()) {
            throw new DataNotFoundException("One or more products in the payload were not found");
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

        notifySse(trxId, request.getStatus() != null ? request.getStatus() : "PAID", payload.getTotal());
        transactionCacheService.evict(trxId, merchantId);
        transactionCacheService.markAsProcessed(trxId, userId);
    }
    
    @Override
    public Page<ResTransactionHistoryDto> getTransactionHistory(
            UUID profileId, LocalDate startDate, LocalDate endDate, ReqPaginationDto paginationDto, boolean isMerchant
    ) {
        
        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : LocalDateTime.now().minusDays(30);
        LocalDateTime end = (endDate != null) ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        if (start.isAfter(end)) {
            throw new BadRequestException("startDate must be before or equal to endDate");
        }
        
        int size = paginationDto.getSize() != null ? paginationDto.getSize() : 10;
        int page = paginationDto.getPage() != null ? paginationDto.getPage() : 0;

        String sortBy = (paginationDto.getSortBy() != null && !paginationDto.getSortBy().isBlank()) 
                ? paginationDto.getSortBy() : "createdAt";
        Sort.Direction direction = (paginationDto.getSortDir() != null && paginationDto.getSortDir().equalsIgnoreCase("ASC")) 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<AccountTransactionEntity> transactionPage;

        if (isMerchant) {
            transactionPage = accountTransactionRepository.findAllByReceiverIdAndCreatedAtBetween(profileId, start, end, pageable);
        } else {
            transactionPage = accountTransactionRepository.findAllByRequesterIdAndCreatedAtBetween(profileId, start, end, pageable);
        }

        return transactionPage.map(entity -> transactionMapper.toHistoryResponse(entity, isMerchant));
    }

    private void notifySse(String trxId, String status, Long total) {
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("trx_id", trxId);
        payload.put("status", status);
        payload.put("total", total);
        
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
