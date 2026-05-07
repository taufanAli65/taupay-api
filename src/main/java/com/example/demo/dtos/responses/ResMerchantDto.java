package com.example.demo.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResMerchantDto {
    private UUID id;
    private String name;
    private UUID category_id;
    private Boolean is_active;
}
