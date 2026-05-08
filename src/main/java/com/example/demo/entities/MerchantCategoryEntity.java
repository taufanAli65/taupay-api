package com.example.demo.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mst_merchant_categories")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MerchantCategoryEntity extends BaseEntity {
    @Column(nullable = false)
    private String name;
}
