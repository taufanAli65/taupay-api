package com.example.demo.dtos.responses;

import lombok.Data;

import java.util.UUID;

@Data
public class ResProductCategoryDto {
    private UUID id;
    private String name;
    private UUID merchantId;
    private String merchantName;
}
