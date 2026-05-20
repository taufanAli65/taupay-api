package com.example.demo.services.impl;

import com.example.demo.dtos.requests.ReqChangePinDto;
import com.example.demo.dtos.requests.ReqMerchantDto;
import com.example.demo.dtos.requests.ReqMerchantFilterDto;
import com.example.demo.dtos.requests.ReqMerchantStatusDto;
import com.example.demo.dtos.requests.ReqRegisterMerchantDto;
import com.example.demo.dtos.responses.ResCommonStatisticsDto;
import com.example.demo.dtos.responses.ResMerchantDashboardDto;
import com.example.demo.dtos.responses.ResMerchantDto;
import com.example.demo.dtos.responses.ResTopProductDto;
import com.example.demo.dtos.responses.ResDailyRevenueDto;
import com.example.demo.dtos.responses.ResDashboardFinancialDto;
import com.example.demo.dtos.responses.ResLowStockProductDto;
import com.example.demo.entities.*;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.exceptions.DuplicateResourceException;
import com.example.demo.mappers.MerchantDashboardMapper;
import com.example.demo.mappers.MerchantMapper;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.MerchantCategoryRepository;
import com.example.demo.repositories.MerchantRepository;
import com.example.demo.repositories.AccountTransactionRepository;
import com.example.demo.repositories.AccountProductTransactionRepository;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.repositories.WalletRepository;
import com.example.demo.repositories.specs.MerchantSpecification;
import com.example.demo.services.MerchantService;
import com.example.demo.services.TransactionCacheService;
import com.example.demo.services.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MerchantServiceImpl implements MerchantService {
    private final AccountRepository accountRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantCategoryRepository merchantCategoryRepository;
    private final AccountTransactionRepository accountTransactionRepository;
    private final AccountProductTransactionRepository accountProductTransactionRepository;
    private final ProductRepository productRepository;
    private final WalletService walletService;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final MerchantMapper merchantMapper;
    private final MerchantDashboardMapper merchantDashboardMapper;
    private final TransactionCacheService transactionCacheService;

    @Override
    @Transactional
    public ResMerchantDto createMerchant(ReqRegisterMerchantDto request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already used");
        }

        MerchantCategoryEntity category = findCategoryById(request.getCategoryId());

        AccountEntity account = new AccountEntity();
        account.setEmail(request.getEmail());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setRole(RoleEnum.MERCHANT);
        AccountEntity savedAccount = accountRepository.save(account);

        MerchantEntity merchant = new MerchantEntity();
        merchant.setAccount(savedAccount);
        merchant.setName(request.getName());
        merchant.setAddress(request.getAddress());
        merchant.setCategory(category);
        merchant.setIsActive(true);

        MerchantEntity savedMerchant = merchantRepository.save(merchant);
        
        // Automatically create wallet for merchant
        walletService.createWallet(savedMerchant.getId(), OwnerTypeEnum.MERCHANT);

        return merchantMapper.toResponse(savedMerchant);
    }

    @Override
    public ResMerchantDashboardDto getMerchantDashboard(UUID merchantId) {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEndExclusive = today.plusDays(1).atStartOfDay();

        LocalDate yesterday = today.minusDays(1);
        LocalDateTime yesterdayStart = yesterday.atStartOfDay();
        LocalDateTime yesterdayEndExclusive = todayStart;

        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime sevenDaysAgo = LocalDate.now().minusDays(6).atStartOfDay();

        Long todayRevenue = accountTransactionRepository.sumAmountByReceiverIdAndCreatedAtBetween(merchantId, todayStart, todayEndExclusive);
        Long todayOrders = accountTransactionRepository.countByReceiverIdAndCreatedAtBetween(merchantId, todayStart, todayEndExclusive);
        Long yesterdayRevenue = accountTransactionRepository.sumAmountByReceiverIdAndCreatedAtBetween(merchantId, yesterdayStart, yesterdayEndExclusive);
        Long yesterdayOrders = accountTransactionRepository.countByReceiverIdAndCreatedAtBetween(merchantId, yesterdayStart, yesterdayEndExclusive);

        if (todayRevenue == null) todayRevenue = 0L;
        if (todayOrders == null) todayOrders = 0L;
        if (yesterdayRevenue == null) yesterdayRevenue = 0L;
        if (yesterdayOrders == null) yesterdayOrders = 0L;

        Long averageOrderValue = todayOrders > 0 ? (todayRevenue / todayOrders) : 0L;

        Long activeProducts = productRepository.countByMerchantIdAndIsActiveTrue(merchantId);
        Long totalProducts = productRepository.countByMerchantId(merchantId);
        Long deactivatedProducts = productRepository.countByMerchantIdAndIsActiveFalse(merchantId);

        // Top products this month
        List<Object[]> tops = accountProductTransactionRepository.findTopProductsByMerchantInPeriod(merchantId, monthStart, java.time.LocalDateTime.now(), org.springframework.data.domain.PageRequest.of(0, 5));
        List<ResTopProductDto> topProducts = merchantDashboardMapper.toTopProducts(tops);

        // Daily revenue for last 7 days
        List<Object[]> daily = accountTransactionRepository.sumDailyByReceiverSince(merchantId, sevenDaysAgo);
        Map<String, Long> dailyMap = new HashMap<>();
        if (daily != null) {
            for (Object[] r : daily) {
                if (r == null || r.length < 2) continue;
                String dateStr = r[0] != null ? r[0].toString() : null;
                Long sum = r[1] != null ? ((Number) r[1]).longValue() : 0L;
                dailyMap.put(dateStr, sum);
            }
        }
        List<ResDailyRevenueDto> trend = merchantDashboardMapper.toDailyRevenueTrend(dailyMap, LocalDate.now().minusDays(6), 7);

        // Low stock products
        Page<ProductEntity> lowStockPage = productRepository.findAllByMerchantIdAndIsActiveTrueAndQuantityEntityStockLessThan(merchantId, 5, PageRequest.of(0, 10));
        List<ResLowStockProductDto> lowStockProducts = merchantDashboardMapper.toLowStockProducts(lowStockPage != null ? lowStockPage.getContent() : null);

        ResDashboardFinancialDto financial = merchantDashboardMapper.toFinancial(
                todayRevenue,
                todayOrders,
                yesterdayRevenue,
                yesterdayOrders,
                averageOrderValue,
                activeProducts,
                totalProducts,
                deactivatedProducts,
                lowStockPage != null ? lowStockPage.getTotalElements() : 0L
        );

        ResMerchantDashboardDto result = new ResMerchantDashboardDto();
        result.setFinancial(financial);
        result.setRevenueTrend(trend);
        result.setTopProducts(topProducts);
        result.setLowStockProducts(lowStockProducts);

        return result;
    }

    @Override
    public Page<ResMerchantDto> findAllMerchants(ReqMerchantFilterDto filterDto) {
        int size = filterDto.getSize() != null ? filterDto.getSize() : 10;
        int page = filterDto.getPage() != null ? filterDto.getPage() : 0;

        PageRequest pageRequest;
        if (filterDto.getSortBy() != null && !filterDto.getSortBy().isBlank()) {
            Sort.Direction direction = (filterDto.getSortDir() != null && filterDto.getSortDir().equalsIgnoreCase("ASC"))
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
            pageRequest = PageRequest.of(page, size, Sort.by(direction, filterDto.getSortBy()));
        } else {
            pageRequest = PageRequest.of(page, size);
        }

        Specification<MerchantEntity> spec = MerchantSpecification.filterBy(filterDto);
        return merchantRepository.findAll(spec, pageRequest).map(merchantMapper::toResponse);
    }

    @Override
    public ResMerchantDto getMerchantById(UUID merchantId) {
        MerchantEntity merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new DataNotFoundException("Merchant with ID: " + merchantId + " not found"));
        
        ResMerchantDto response = merchantMapper.toResponse(merchant);
        
        // Fetch balance
        walletRepository.findByOwnerIdAndOwnerType(merchantId, OwnerTypeEnum.MERCHANT)
                .ifPresent(wallet -> response.setBalance(wallet.getAmount()));
                
        return response;
    }

    @Override
    @Transactional
    public ResMerchantDto updateMerchantById(UUID merchantId, ReqMerchantDto request) {
        MerchantEntity merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new DataNotFoundException("Merchant with ID: " + merchantId + " not found"));
        MerchantCategoryEntity category = findCategoryById(request.getCategoryId());

        merchant.setName(request.getName());
        merchant.setAddress(request.getAddress());
        merchant.setCategory(category);

        return merchantMapper.toResponse(merchantRepository.save(merchant));
    }

    @Override
    @Transactional
    public void changePin(UUID merchantId, ReqChangePinDto request) {
        MerchantEntity merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new DataNotFoundException("Merchant with ID: " + merchantId + " not found"));
        AccountEntity account = merchant.getAccount();
        if (account == null) {
            throw new DataNotFoundException("Account not found for merchant: " + merchantId);
        }

        // If merchant already has a PIN, validate the old one
        if (account.getPin() != null && !account.getPin().isBlank()) {
            if (request.getOldPin() == null || request.getOldPin().isBlank()) {
                throw new BadRequestException("Current PIN is required to set a new one");
            }
            if (!passwordEncoder.matches(request.getOldPin(), account.getPin())) {
                throw new BadRequestException("Current PIN is incorrect");
            }
        }

        // Encode and save new PIN
        account.setPin(passwordEncoder.encode(request.getNewPin()));
        merchantRepository.save(merchant);
    }

    @Override
    @Transactional
    public ResMerchantDto updateMerchantStatus(UUID merchantId, ReqMerchantStatusDto request) {
        MerchantEntity merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new DataNotFoundException("Merchant with ID: " + merchantId + " not found"));
        merchant.setIsActive(request.getIsActive());
        // Persist the status change first. Eviction will run after the surrounding transaction commits
        MerchantEntity saved = merchantRepository.save(merchant);
        if (Boolean.FALSE.equals(request.getIsActive())) {
            // Register after-commit eviction so that no new transactions created in the same transaction slip through
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        transactionCacheService.evictByMerchantId(merchantId);
                    } catch (Exception ex) {
                        log.warn("Failed to evict transaction cache after merchant deactivation: merchantId={}", merchantId, ex);
                    }
                }
            });
        }
        return merchantMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void lockPayments(UUID merchantId, LocalDateTime lockedUntil) {
        MerchantEntity merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new DataNotFoundException("Merchant with ID: " + merchantId + " not found"));
        merchant.setPaymentLockedUntil(lockedUntil);
        merchantRepository.save(merchant);
    }

    @Override
    public ResCommonStatisticsDto getAdminMerchantStatistics() {
        return ResCommonStatisticsDto.builder()
                .total(merchantRepository.count())
                .active(merchantRepository.countByIsActiveTrue())
                .deactivated(merchantRepository.countByIsActiveFalse())
                .build();
    }

    private MerchantCategoryEntity findCategoryById(UUID categoryId) {
        return merchantCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new DataNotFoundException("Merchant category not found"));
    }
}
