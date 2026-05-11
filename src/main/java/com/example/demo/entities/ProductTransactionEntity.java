package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mst_product_transaction_histories")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductTransactionEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private ProductEntity product;

    @Column(name = "product_name_at_time", nullable = false)
    private String productName;

    @Column(name = "product_price_at_time", nullable = false)
    private Long productPrice;

    @Column(nullable = false)
    private Integer quantity;
}
