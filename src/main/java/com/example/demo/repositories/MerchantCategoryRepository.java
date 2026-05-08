package com.example.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import com.example.demo.entities.MerchantCategoryEntity;


public interface MerchantCategoryRepository extends JpaRepository<MerchantCategoryEntity, UUID> {
    
}
