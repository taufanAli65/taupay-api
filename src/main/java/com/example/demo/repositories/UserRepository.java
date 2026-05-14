package com.example.demo.repositories;

import com.example.demo.dtos.responses.ResUserDto;
import com.example.demo.entities.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {
    @Query(value = "select new com.example.demo.dtos.responses.ResUserDto( " +
            "u.id, " +
            "u.firstName, " +
            "u.lastName, " +
            "a.email, " +
            "u.address, " +
            "u.birthDate, " +
            "u.isActive ) " +
            "from UserEntity u " +
            "join u.account a " +
            "where u.id = :userId")
    Optional<ResUserDto> findUserById(@Param("userId") UUID userId);

    @EntityGraph(attributePaths = {"account"})
    Page<UserEntity> findAll(Specification<UserEntity> spec, Pageable pageable);

    @Query(value = "select new com.example.demo.dtos.responses.ResUserDto( " +
            "u.id, " +
            "u.firstName, " +
            "u.lastName, " +
            "a.email, " +
            "u.address, " +
            "u.birthDate, " +
            "u.isActive ) " +
            "from UserEntity u " +
            "join u.account a",
            countQuery = "select count(u) from UserEntity u")
    Page<ResUserDto> findAllUsers(Pageable pageable);

    Optional<UserEntity> findByAccountId(UUID id);
}
