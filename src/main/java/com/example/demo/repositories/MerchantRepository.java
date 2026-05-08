package com.example.demo.repositories;

import com.example.demo.entities.MerchantEntity;
import com.example.demo.dtos.responses.ResMerchantDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MerchantRepository extends JpaRepository<MerchantEntity, UUID> {
    Optional<MerchantEntity> findByAccountId(UUID accountId);

    @Query(value = "select new com.example.demo.dtos.responses.ResMerchantDto(" +
           "m.id, m.name, mc.name, m.isActive) " +
           "from MerchantEntity m " +
           "join m.merchantCategory mc " +
           "order by m.name asc",
           countQuery = "select count(m) from MerchantEntity m join m.merchantCategory mc") // TODO: optimize count query since it currently need to scan all table just to count total data
    Page<ResMerchantDto> findAllMerchants(Pageable pageable);

    @Query("select new com.example.demo.dtos.responses.ResMerchantDto(" +
           "m.id, m.name, mc.name, m.isActive) " +
           "from MerchantEntity m " +
           "join m.merchantCategory mc " +
           "where m.id = :merchantId")
    Optional<ResMerchantDto> getMerchantById(UUID merchantId);
}
