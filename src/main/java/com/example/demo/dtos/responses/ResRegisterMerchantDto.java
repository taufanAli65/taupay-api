package com.example.demo.dtos.responses;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ResRegisterMerchantDto {
    private UUID id;
    private String name;
    private String email;
    private String address;
}
