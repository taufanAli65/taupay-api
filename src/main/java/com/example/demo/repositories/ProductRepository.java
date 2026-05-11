package com.example.demo.repositories;

import com.example.demo.entities.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {
    Page<ProductEntity> findAllByMerchantIdAndIsActiveTrue(UUID merchantId, Pageable pageable);

    Page<ProductEntity> findAllByIsActiveTrue(Pageable pageable);

    Optional<ProductEntity> findByIdAndMerchantIdAndIsActiveTrue(UUID id, UUID merchantId);

    Optional<ProductEntity> findByIdAndMerchantId(UUID id, UUID merchantId);

    Optional<ProductEntity> findByIdAndIsActiveTrue(UUID id);
}
