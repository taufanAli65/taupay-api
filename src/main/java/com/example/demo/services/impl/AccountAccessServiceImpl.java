package com.example.demo.services.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entities.AccountEntity;
import com.example.demo.entities.MerchantEntity;
import com.example.demo.entities.RoleEnum;
import com.example.demo.entities.UserEntity;
import com.example.demo.exceptions.AccountLockedException;
import com.example.demo.exceptions.UnauthorizedException;
import com.example.demo.repositories.MerchantRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.AccountAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountAccessServiceImpl implements AccountAccessService {
    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;

    @Override
    @Transactional
    public void assertCanLogin(AccountEntity account) {
        if (account == null) {
            return;
        }

        if (account.getRole() == RoleEnum.USER) {
            UserEntity user = account.getUser();
            if (user == null) {
                return;
            }
            clearExpiredUserLock(user);
            if (isStillLocked(user.getPaymentLockedUntil())) {
                throw new AccountLockedException("User account is temporarily locked until " + user.getPaymentLockedUntil());
            }
            assertActive(user.getIsActive(), "User account is inactive");
            return;
        }

        if (account.getRole() == RoleEnum.MERCHANT) {
            MerchantEntity merchant = account.getMerchant();
            if (merchant == null) {
                return;
            }
            clearExpiredMerchantLock(merchant);
            if (isStillLocked(merchant.getPaymentLockedUntil())) {
                throw new AccountLockedException("Merchant account is temporarily locked until " + merchant.getPaymentLockedUntil());
            }
            assertActive(merchant.getIsActive(), "Merchant account is inactive");
        }
    }

    @Override
    @Transactional
    public void assertCanAccessPayments(AccountEntity account) {
        if (account == null) {
            return;
        }

        if (account.getRole() == RoleEnum.USER) {
            UserEntity user = account.getUser();
            if (user == null) {
                return;
            }
            clearExpiredUserLock(user);
            if (isStillLocked(user.getPaymentLockedUntil())) {
                throw new AccountLockedException("User account is temporarily locked for payments until " + user.getPaymentLockedUntil());
            }
            assertActive(user.getIsActive(), "User account is inactive");
            return;
        }

        if (account.getRole() == RoleEnum.MERCHANT) {
            MerchantEntity merchant = account.getMerchant();
            if (merchant == null) {
                return;
            }
            clearExpiredMerchantLock(merchant);
            if (isStillLocked(merchant.getPaymentLockedUntil())) {
                throw new AccountLockedException("Merchant account is temporarily locked for payments until " + merchant.getPaymentLockedUntil());
            }
            assertActive(merchant.getIsActive(), "Merchant account is inactive");
        }
    }

    private void assertActive(Boolean isActive, String message) {
        if (!Boolean.TRUE.equals(isActive)) {
            throw new UnauthorizedException(message);
        }
    }

    private boolean isStillLocked(LocalDateTime lockedUntil) {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    private void clearExpiredUserLock(UserEntity user) {
        if (user.getPaymentLockedUntil() != null && !isStillLocked(user.getPaymentLockedUntil())) {
            user.setPaymentLockedUntil(null);
            if (!Boolean.TRUE.equals(user.getIsActive())) {
                user.setIsActive(true);
            }
            userRepository.save(user);
        }
    }

    private void clearExpiredMerchantLock(MerchantEntity merchant) {
        if (merchant.getPaymentLockedUntil() != null && !isStillLocked(merchant.getPaymentLockedUntil())) {
            merchant.setPaymentLockedUntil(null);
            if (!Boolean.TRUE.equals(merchant.getIsActive())) {
                merchant.setIsActive(true);
            }
            merchantRepository.save(merchant);
        }
    }
}
