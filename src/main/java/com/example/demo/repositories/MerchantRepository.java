package com.example.demo.repositories;

import com.example.demo.entities.MerchantEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MerchantRepository extends JpaRepository<MerchantEntity, UUID> {
    @EntityGraph(attributePaths = {"account", "category"})
    Optional<MerchantEntity> findByAccountId(UUID accountId);

    @EntityGraph(attributePaths = {"account", "category"})
    Page<MerchantEntity> findAllByOrderByNameAsc(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"account", "category"})
    Optional<MerchantEntity> findById(UUID merchantId);
}
