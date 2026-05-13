package com.example.demo.repositories;

import com.example.demo.entities.AccountTransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface AccountTransactionRepository extends JpaRepository<AccountTransactionEntity, UUID> {
    
    @EntityGraph(attributePaths = {"receiver"})
    Page<AccountTransactionEntity> findAllByRequesterIdAndCreatedAtBetween(
            UUID requesterId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    @EntityGraph(attributePaths = {"requester"})
    Page<AccountTransactionEntity> findAllByReceiverIdAndCreatedAtBetween(
            UUID receiverId, LocalDateTime start, LocalDateTime end, Pageable pageable);
}
