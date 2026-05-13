package com.example.demo.services;

import com.example.demo.entities.OwnerTypeEnum;
import com.example.demo.entities.WalletEntity;

import java.util.UUID;

public interface WalletService {
    WalletEntity createWallet(UUID ownerId, OwnerTypeEnum ownerType);
}
