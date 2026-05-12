package com.example.demo.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entities.AccountProductTransactionEntity;

public interface AccountProductTransactionRepository extends JpaRepository<AccountProductTransactionEntity, UUID> {

}
