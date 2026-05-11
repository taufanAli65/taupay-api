package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mst_product_categories")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategoryEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", referencedColumnName = "id")
    private MerchantEntity merchant;

    @Column(nullable = false)
    private String name;

}
