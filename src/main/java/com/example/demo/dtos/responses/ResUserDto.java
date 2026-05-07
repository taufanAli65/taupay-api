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
    private String first_name;
    private String last_name;
    private String address;
    private LocalDate birth_date;
    private Boolean is_active;
    private MerchantEntity merchant;
}
