package com.example.demo.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entities.ProductQuantityEntity;

public interface ProductQuantityRepository extends JpaRepository<ProductQuantityEntity, UUID> {
    boolean existsByProductId(UUID productId);

    @Modifying
    @Query("""
        update ProductQuantityEntity pq
        set pq.stock = pq.stock - :quantity
        where pq.product.id = :productId
          and pq.stock >= :quantity
    """)
    int decrementStockIfEnough(@Param("productId") UUID productId, @Param("quantity") Integer quantity);
}
