package com.example.demo.repositories;

import com.example.demo.entities.ProductCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategoryEntity, UUID> {
    List<ProductCategoryEntity> findAllByMerchantId(UUID merchantId);

    Optional<ProductCategoryEntity> findByIdAndMerchantId(UUID id, UUID merchantId);
}
