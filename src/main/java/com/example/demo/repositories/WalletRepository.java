package com.example.demo.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entities.OwnerTypeEnum;
import com.example.demo.entities.WalletEntity;

public interface WalletRepository extends JpaRepository<WalletEntity, UUID> {
    Optional<WalletEntity> findByOwnerIdAndOwnerType(UUID ownerId, OwnerTypeEnum ownerType);

    boolean existsByNoWallet(String noWallet);
}
