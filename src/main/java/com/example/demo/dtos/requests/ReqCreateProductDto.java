package com.example.demo.dtos.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ReqCreateProductDto {
    @NotBlank(message = "Product name is required")
    private String name;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price cannot be negative")
    private Long price;

    private String description;

    private UUID categoryId;
}
