package com.example.demo.dtos.requests;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqMerchantDto {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @NotBlank(message = "Address is required")
    private String address;
}
