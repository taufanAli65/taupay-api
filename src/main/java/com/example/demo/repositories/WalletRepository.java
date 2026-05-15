package com.example.demo.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entities.OwnerTypeEnum;
import com.example.demo.entities.WalletEntity;

public interface WalletRepository extends JpaRepository<WalletEntity, UUID> {
    Optional<WalletEntity> findByOwnerIdAndOwnerType(UUID ownerId, OwnerTypeEnum ownerType);
    boolean existsByOwnerIdAndOwnerType(UUID ownerId, OwnerTypeEnum ownerType);

    boolean existsByNoWallet(String noWallet);

    @Modifying
    @Query("""
        update WalletEntity w
        set w.amount = coalesce(w.amount, 0) - :amount
        where w.ownerId = :ownerId
          and w.ownerType = :ownerType
          and coalesce(w.amount, 0) >= :amount
    """)
    int decrementAmountIfEnough(
            @Param("ownerId") UUID ownerId,
            @Param("ownerType") OwnerTypeEnum ownerType,
            @Param("amount") Long amount
    );

    @Modifying
    @Query("""
        update WalletEntity w
        set w.amount = coalesce(w.amount, 0) + :amount
        where w.ownerId = :ownerId
          and w.ownerType = :ownerType
    """)
    int incrementAmount(
            @Param("ownerId") UUID ownerId,
            @Param("ownerType") OwnerTypeEnum ownerType,
            @Param("amount") Long amount
    );
}
