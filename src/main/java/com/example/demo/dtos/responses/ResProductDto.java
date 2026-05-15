package com.example.demo.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    private Integer stock;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ResMerchantDto merchant;

    private ResProductCategoryDto category;
}
