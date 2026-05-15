package com.example.demo.repositories;

import com.example.demo.entities.MerchantCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface MerchantCategoryRepository extends JpaRepository<MerchantCategoryEntity, UUID> {
    List<MerchantCategoryEntity> findByNameContainingIgnoreCaseOrderByNameAsc(String name);
    List<MerchantCategoryEntity> findAllByOrderByNameAsc();
}
