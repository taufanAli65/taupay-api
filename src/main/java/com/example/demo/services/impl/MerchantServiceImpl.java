package com.example.demo.services.impl;

import com.example.demo.dtos.requests.ReqMerchantDto;
import com.example.demo.dtos.requests.ReqMerchantFilterDto;
import com.example.demo.dtos.requests.ReqMerchantStatusDto;
import com.example.demo.dtos.requests.ReqRegisterMerchantDto;
import com.example.demo.dtos.responses.ResCommonStatisticsDto;
import com.example.demo.dtos.responses.ResMerchantDto;
import com.example.demo.dtos.responses.ResTopProductDto;
import com.example.demo.dtos.responses.ResDailyRevenueDto;
import com.example.demo.dtos.responses.ResDashboardFinancialDto;
import com.example.demo.dtos.responses.ResLowStockProductDto;
import com.example.demo.dtos.responses.ResMerchantDashboardDto;
import com.example.demo.entities.AccountEntity;
import com.example.demo.entities.MerchantCategoryEntity;
import com.example.demo.entities.MerchantEntity;
import com.example.demo.entities.OwnerTypeEnum;
import com.example.demo.entities.ProductEntity;
import com.example.demo.entities.RoleEnum;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.exceptions.DuplicateResourceException;
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
import com.example.demo.services.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MerchantServiceImpl implements MerchantService {
    private final AccountRepository accountRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantCategoryRepository merchantCategoryRepository;
    private final AccountTransactionRepository accountTransactionRepository;
    private final AccountProductTransactionRepository accountProductTransactionRepository;
    private final ProductRepository productRepository;
    private final WalletService walletService;
    private final PasswordEncoder passwordEncoder;
    private final MerchantMapper merchantMapper;
    private final WalletRepository walletRepository;

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
        List<ResTopProductDto> topProducts = new ArrayList<>();
        List<Object[]> tops = accountProductTransactionRepository.findTopProductsByMerchantInPeriod(merchantId, monthStart, java.time.LocalDateTime.now(), org.springframework.data.domain.PageRequest.of(0, 5));
        if (tops != null) {
            for (Object[] row : tops) {
                ResTopProductDto dto = new ResTopProductDto();
                dto.setProductName(row[0] != null ? row[0].toString() : null);
                dto.setTotalQuantity(row[1] != null ? ((Number) row[1]).longValue() : 0L);
                dto.setTotalRevenue(row[2] != null ? ((Number) row[2]).longValue() : 0L);
                topProducts.add(dto);
            }
        }

        // Daily revenue for last 7 days
        List<ResDailyRevenueDto> trend = new ArrayList<>();
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
        for (int i = 0; i < 7; i++) {
            LocalDate d = LocalDate.now().minusDays(6 - i);
            String key = d.toString();
            ResDailyRevenueDto p = new ResDailyRevenueDto();
            p.setDate(key);
            p.setRevenue(dailyMap.getOrDefault(key, 0L));
            trend.add(p);
        }

        // Low stock products
        Page<ProductEntity> lowStockPage = productRepository.findAllByMerchantIdAndIsActiveTrueAndQuantityEntityStockLessThan(merchantId, 5, PageRequest.of(0, 10));
        List<ResLowStockProductDto> lowStockProducts = new ArrayList<>();
        if (lowStockPage != null) {
            for (ProductEntity p : lowStockPage.getContent()) {
                ResLowStockProductDto item = new ResLowStockProductDto();
                item.setProductId(p.getId().toString());
                item.setProductName(p.getName());
                item.setStock(p.getQuantityEntity() != null ? p.getQuantityEntity().getStock() : 0);
                lowStockProducts.add(item);
            }
        }

        ResDashboardFinancialDto financial = new ResDashboardFinancialDto();
        financial.setTodayRevenue(todayRevenue);
        financial.setTodayOrders(todayOrders);
        financial.setYesterdayRevenue(yesterdayRevenue);
        financial.setYesterdayOrders(yesterdayOrders);
        financial.setAverageOrderValue(averageOrderValue);
        financial.setActiveProducts(activeProducts);
        financial.setTotalProducts(totalProducts);
        financial.setDeactivatedProducts(deactivatedProducts);
        financial.setLowStockCount(lowStockPage != null ? lowStockPage.getTotalElements() : 0L);

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
        if (request.getPin() != null) {
            AccountEntity account = merchant.getAccount();
            if (account == null) {
                throw new DataNotFoundException("Account for merchant with ID: " + merchantId + " not found");
            }
            account.setPin(passwordEncoder.encode(request.getPin()));
        }

        return merchantMapper.toResponse(merchantRepository.save(merchant));
    }

    @Override
    @Transactional
    public ResMerchantDto updateMerchantStatus(UUID merchantId, ReqMerchantStatusDto request) {
        MerchantEntity merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new DataNotFoundException("Merchant with ID: " + merchantId + " not found"));
        merchant.setIsActive(request.getIsActive());
        return merchantMapper.toResponse(merchantRepository.save(merchant));
        // TODO: INVALIDATE MERCHANT SESSION UNTILL ADMIN RE-ACTIVATE THE MERCHANT ACCOUNT OR TTL FOR DEACTIVATION
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
