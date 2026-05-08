package com.example.demo.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResRegisterMerchantDto {
    private UUID id;
    private String name;
    private String email;
    private String address;
    private String category;
}
