package com.example.demo.repositories;

import com.example.demo.entities.MerchantCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MerchantCategoryRepository extends JpaRepository<MerchantCategoryEntity, UUID> {
}
