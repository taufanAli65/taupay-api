package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lnk_account_product_transaction_histories")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountProductTransactionEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_transaction_history_id", referencedColumnName = "id")
    private AccountTransactionEntity accountTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_transaction_history_id", referencedColumnName = "id")
    private ProductTransactionEntity productTransaction;
}
