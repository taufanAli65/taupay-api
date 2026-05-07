package com.example.demo.dtos.responses;

import com.example.demo.entities.MerchantEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResUserDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String address;
    private LocalDate birthDate;
    private Boolean isActive;
    private MerchantEntity merchant;
}
