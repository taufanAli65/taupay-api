package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

@Entity
@Table(name = "mst_account_transaction_histories")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountTransactionEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", referencedColumnName = "id")
    private MerchantEntity receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", referencedColumnName = "id")
    private UserEntity requester;

    @Column(nullable = false)
    private BigInteger amount;

    @Column(name = "is_success", nullable = false)
    private Boolean isSuccess;

    @Column(nullable = false)
    private String category;
}
