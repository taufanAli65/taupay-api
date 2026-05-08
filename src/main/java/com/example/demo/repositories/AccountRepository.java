package com.example.demo.repositories;

import com.example.demo.entities.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {
    boolean existsByEmail(String email);

    Optional<AccountEntity> findByEmail(String email);

}
