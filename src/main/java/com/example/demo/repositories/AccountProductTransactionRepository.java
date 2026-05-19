package com.example.demo.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entities.AccountProductTransactionEntity;

public interface AccountProductTransactionRepository extends JpaRepository<AccountProductTransactionEntity, UUID> {

	@Query("SELECT pt.productName, SUM(pt.quantity), SUM(pt.productPrice * pt.quantity) " +
	"FROM AccountProductTransactionEntity l " +
	"JOIN l.productTransaction pt " +
	"JOIN l.accountTransaction at " +
	"WHERE at.receiver.id = :merchantId AND at.isSuccess = true AND at.createdAt >= :start AND at.createdAt < :end " +
	"GROUP BY pt.productName " +
	"ORDER BY SUM(pt.quantity) DESC")
    List<Object[]> findTopProductsByMerchantInPeriod(@Param("merchantId") UUID merchantId,
						     @Param("start") LocalDateTime start,
						     @Param("end") LocalDateTime end,
						     Pageable pageable);
}
