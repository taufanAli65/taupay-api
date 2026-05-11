package com.example.demo.dtos.responses;

import lombok.Data;

import java.util.UUID;

@Data
public class ResProductDto {
    private UUID id;
    private String name;
    private Long price;
    private String description;
    private Boolean isActive;
    private String imageUrl;
    private ResMerchantDto merchant;
    private ResProductCategoryDto category;
}
