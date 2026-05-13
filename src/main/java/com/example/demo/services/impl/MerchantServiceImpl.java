package com.example.demo.services.impl;

import com.example.demo.dtos.requests.ReqMerchantDto;
import com.example.demo.dtos.requests.ReqMerchantStatusDto;
import com.example.demo.dtos.requests.ReqRegisterMerchantDto;
import com.example.demo.dtos.responses.ResMerchantDto;
import com.example.demo.entities.*;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.exceptions.DuplicateResourceException;
import com.example.demo.mappers.MerchantMapper;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.MerchantCategoryRepository;
import com.example.demo.repositories.MerchantRepository;
import com.example.demo.services.MerchantService;
import com.example.demo.services.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MerchantServiceImpl implements MerchantService {
    private final AccountRepository accountRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantCategoryRepository merchantCategoryRepository;
    private final WalletService walletService;
    private final PasswordEncoder passwordEncoder;
    private final MerchantMapper merchantMapper;

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
    public Page<ResMerchantDto> findAllMerchants(int size, int page) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return merchantRepository.findAllByOrderByNameAsc(pageRequest).map(merchantMapper::toResponse);
    }

    @Override
    public ResMerchantDto getMerchantById(UUID merchantId) {
        MerchantEntity merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new DataNotFoundException("Merchant with ID: " + merchantId + " not found"));
        return merchantMapper.toResponse(merchant);
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
    public ResMerchantDto updateMerchantStatus(UUID merchantId, ReqMerchantStatusDto request) {
        MerchantEntity merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new DataNotFoundException("Merchant with ID: " + merchantId + " not found"));
        merchant.setIsActive(request.getIsActive());
        return merchantMapper.toResponse(merchantRepository.save(merchant));
        // TODO: INVALIDATE MERCHANT SESSION UNTILL ADMIN RE-ACTIVATE THE MERCHANT ACCOUNT OR TTL FOR DEACTIVATION
    }

    private MerchantCategoryEntity findCategoryById(UUID categoryId) {
        return merchantCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new DataNotFoundException("Merchant category not found"));
    }
}
