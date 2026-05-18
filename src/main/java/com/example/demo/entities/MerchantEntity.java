package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mst_merchants")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MerchantEntity extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private AccountEntity account;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private MerchantCategoryEntity category;

    private String address;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "payment_locked_until")
    private LocalDateTime paymentLockedUntil;
}
