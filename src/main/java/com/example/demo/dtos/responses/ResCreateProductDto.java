package com.example.demo.dtos.responses;

import lombok.Data;

import java.util.UUID;

@Data
public class ResCreateProductDto {
    private UUID id;
    private String name;
    private Long price;
    private String description;
    private Boolean isActive;
    private UUID merchantId;
    private UUID categoryId;
    private String categoryName;
    private Integer stock;
}
