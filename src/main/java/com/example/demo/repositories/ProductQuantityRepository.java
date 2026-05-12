package com.example.demo.repositories;

import com.example.demo.entities.ProductQuantityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductQuantityRepository extends JpaRepository<ProductQuantityEntity, UUID> {
    Optional<ProductQuantityEntity> findByProductId(UUID productId);
}
