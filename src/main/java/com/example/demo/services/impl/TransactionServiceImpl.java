package com.example.demo.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.demo.dtos.requests.ReqTransactionCallbackDto;
import com.example.demo.dtos.requests.ReqTransactionDto;
import com.example.demo.dtos.responses.ResTransactionDto;
import com.example.demo.dtos.responses.ResTransactionHistoryDto;
import com.example.demo.entities.AccountProductTransactionEntity;
import com.example.demo.entities.AccountTransactionEntity;
import com.example.demo.entities.MerchantEntity;
import com.example.demo.entities.OwnerTypeEnum;
import com.example.demo.entities.ProductEntity;
import com.example.demo.entities.ProductTransactionEntity;
import com.example.demo.entities.UserEntity;
import com.example.demo.entities.WalletEntity;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.mappers.TransactionMapper;
import com.example.demo.repositories.AccountProductTransactionRepository;
import com.example.demo.repositories.AccountTransactionRepository;
import com.example.demo.repositories.MerchantRepository;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.repositories.ProductTransactionHistoryRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.repositories.WalletRepository;
import com.example.demo.services.EventStreamService;
import com.example.demo.services.TransactionCacheService;
import com.example.demo.services.TransactionService;
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

    @Value("${app.transaction.ttl:5m}")
    private java.time.Duration transactionTtl;

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
        return payload;
    }

    @Override
    public SseEmitter subscribeToTransactionEvents(String trxId) {
        if (trxId == null || trxId.isBlank()) {
            throw new BadRequestException("trx_id is required");
        }

        return eventStreamService.subscribe(trxId, transactionTtl.toMillis());
    }

    @Override
    @Transactional
    public void handlePaymentCallback(ReqTransactionCallbackDto request) {
        if (request == null || request.getTrxId() == null || request.getTrxId().isBlank()) {
            throw new BadRequestException("trx_id is required");
        }

        String status = request.getStatus() != null ? request.getStatus().trim().toUpperCase() : "";
        ResTransactionDto payload = transactionCacheService.get(request.getTrxId());

        if (!"PAID".equals(status)) {
            log.info("Transaction {} completed with status {}", request.getTrxId(), status);
            notifySse(request.getTrxId(), status, payload.getTotal());
            transactionCacheService.evict(request.getTrxId());
            return;
        }

        UUID merchantId = TransactionUtils.parseUuid(payload.getMerchantId(), "merchant_id");
        UUID payerUserId = TransactionUtils.parseUuid(request.getPayerUserId(), "payer_user_id");

        MerchantEntity merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new DataNotFoundException("Merchant not found"));
        UserEntity payer = userRepository.findById(payerUserId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        Long amount = payload.getTotal();

        WalletEntity payerWallet = walletRepository.findByOwnerIdAndOwnerType(payerUserId, OwnerTypeEnum.USER)
            .orElseThrow(() -> new DataNotFoundException("User wallet not found"));
        WalletEntity merchantWallet = walletRepository.findByOwnerIdAndOwnerType(merchantId, OwnerTypeEnum.MERCHANT)
            .orElseThrow(() -> new DataNotFoundException("Merchant wallet not found"));

        long payerBalance = payerWallet.getAmount() != null ? payerWallet.getAmount() : 0L;
        if (payerBalance < amount) {
            throw new BadRequestException("Insufficient wallet balance");
        }
        long merchantBalance = merchantWallet.getAmount() != null ? merchantWallet.getAmount() : 0L;

        payerWallet.setAmount(payerBalance - amount);
        merchantWallet.setAmount(merchantBalance + amount);
        walletRepository.save(payerWallet);
        walletRepository.save(merchantWallet);

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
            ProductEntity product = productRepository.findById(productId)
                    .orElseThrow(() -> new DataNotFoundException("Product not found"));

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

        notifySse(request.getTrxId(), "PAID", payload.getTotal());
        transactionCacheService.evict(request.getTrxId());
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

    private String generateTransactionId() {
        return "TRX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
