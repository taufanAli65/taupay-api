package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "mst_wallets")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WalletEntity extends BaseEntity {
    @Column(name = "no_wallet", nullable = false)
    private String noWallet;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false)
    private OwnerTypeEnum ownerType;

    private Long amount;
}
