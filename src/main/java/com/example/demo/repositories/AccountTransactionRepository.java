package com.example.demo.repositories;

import com.example.demo.entities.AccountTransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AccountTransactionRepository extends JpaRepository<AccountTransactionEntity, UUID> {
    
    @EntityGraph(attributePaths = {"receiver", "items", "items.productTransaction"})
    Page<AccountTransactionEntity> findAllByRequesterIdAndCreatedAtBetween(
            UUID requesterId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    @EntityGraph(attributePaths = {"requester", "items", "items.productTransaction"})
    Page<AccountTransactionEntity> findAllByReceiverIdAndCreatedAtBetween(
            UUID receiverId, LocalDateTime start, LocalDateTime end, Pageable pageable);

        @Query("SELECT SUM(a.amount) FROM AccountTransactionEntity a WHERE a.receiver.id = :receiverId AND a.isSuccess = true AND a.createdAt >= :start AND a.createdAt < :end")
        Long sumAmountByReceiverIdAndCreatedAtBetween(@Param("receiverId") UUID receiverId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

        @Query("SELECT COUNT(a) FROM AccountTransactionEntity a WHERE a.receiver.id = :receiverId AND a.isSuccess = true AND a.createdAt >= :start AND a.createdAt < :end")
        Long countByReceiverIdAndCreatedAtBetween(@Param("receiverId") UUID receiverId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Returns list of [date (as object), sum]
    @Query("SELECT FUNCTION('date', a.createdAt), SUM(a.amount) FROM AccountTransactionEntity a " +
            "WHERE a.receiver.id = :receiverId AND a.isSuccess = true AND a.createdAt >= :start " +
            "GROUP BY FUNCTION('date', a.createdAt) ORDER BY FUNCTION('date', a.createdAt) ASC")
    List<Object[]> sumDailyByReceiverSince(@Param("receiverId") UUID receiverId, @Param("start") LocalDateTime start);
}
