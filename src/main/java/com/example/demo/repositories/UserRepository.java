package com.example.demo.repositories;

import com.example.demo.dtos.responses.ResUserDto;
import com.example.demo.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    @Query("""
            select u.id, u.firstName, u.lastName, u.address, u.birthDate, u.isActive, a.email
            from UserEntity u
            join u.account a
            where u.id = :userId
            """)
    Optional<ResUserDto> findUserById(@Param("userId") UUID userId);
}
