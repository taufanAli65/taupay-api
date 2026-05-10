package com.example.demo.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResMerchantDto {
    private UUID id;
    private String name;
    private String email;
    private String address;
    private UUID categoryId;
    private String categoryName;
    private Boolean active;
}
