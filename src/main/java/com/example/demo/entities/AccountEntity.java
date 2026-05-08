package com.example.demo.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "mst_accounts")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountEntity extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 6)
    private String pin;

    @Column(nullable = false)
    private String role;
}
