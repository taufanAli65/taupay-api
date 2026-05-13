package com.example.demo.services.impl;

import com.example.demo.entities.OwnerTypeEnum;
import com.example.demo.entities.WalletEntity;
import com.example.demo.repositories.WalletRepository;
import com.example.demo.services.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final Random random = new Random();

    @Override
    @Transactional
    public WalletEntity createWallet(UUID ownerId, OwnerTypeEnum ownerType) {
        String prefix = (ownerType == OwnerTypeEnum.USER) ? "10" : "20";
        String noWallet;
        
        // Ensure unique wallet number
        do {
            long randomNumber = 10000000L + random.nextLong(90000000L); // 8 random digits
            noWallet = prefix + randomNumber;
        } while (walletRepository.existsByNoWallet(noWallet));

        WalletEntity wallet = new WalletEntity();
        wallet.setNoWallet(noWallet);
        wallet.setOwnerId(ownerId);
        wallet.setOwnerType(ownerType);
        wallet.setAmount(0L); // Initial balance

        return walletRepository.save(wallet);
    }
}
