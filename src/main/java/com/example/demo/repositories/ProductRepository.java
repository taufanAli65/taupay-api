package com.example.demo.repositories;

import com.example.demo.entities.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, UUID>, JpaSpecificationExecutor<ProductEntity> {
    @EntityGraph(attributePaths = {"merchant.account", "merchant.category", "category", "quantityEntity"})
    Page<ProductEntity> findAllByMerchantIdAndIsActiveTrue(UUID merchantId, Pageable pageable);

    @EntityGraph(attributePaths = {"merchant.account", "merchant.category", "category", "quantityEntity"})
    Page<ProductEntity> findAllByMerchantId(UUID merchantId, Pageable pageable);

    @EntityGraph(attributePaths = {"merchant.account", "merchant.category", "category", "quantityEntity"})
    Page<ProductEntity> findAll(org.springframework.data.jpa.domain.Specification<ProductEntity> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"merchant.account", "merchant.category", "category", "quantityEntity"})
    Page<ProductEntity> findAllByIsActiveTrue(Pageable pageable);

    @Modifying
    @Query("update ProductEntity p set p.category = null where p.category.id = :categoryId")
    void setCategoryToNull(@Param("categoryId") UUID categoryId);

    @EntityGraph(attributePaths = {"merchant.account", "merchant.category", "category", "quantityEntity"})
    Optional<ProductEntity> findByIdAndMerchantIdAndIsActiveTrue(UUID id, UUID merchantId);

    @EntityGraph(attributePaths = {"merchant.account", "merchant.category", "category", "quantityEntity"})
    Optional<ProductEntity> findByIdAndIsActiveTrue(UUID id);

    @EntityGraph(attributePaths = {"merchant.account", "merchant.category", "category", "quantityEntity"})
    Optional<ProductEntity> findByIdAndMerchantId(UUID id, UUID merchantId);

    long countByMerchantId(UUID merchantId);

    long countByMerchantIdAndIsActiveTrue(UUID merchantId);

    long countByMerchantIdAndIsActiveFalse(UUID merchantId);

    long countByIsActiveTrue();

    long countByIsActiveFalse();

    @EntityGraph(attributePaths = {"merchant.account", "merchant.category", "category", "quantityEntity"})
    Page<ProductEntity> findAllByMerchantIdAndIsActiveTrueAndQuantityEntityStockLessThan(UUID merchantId, Integer threshold, Pageable pageable);
}
