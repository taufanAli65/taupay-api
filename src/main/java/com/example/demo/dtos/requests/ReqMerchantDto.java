package com.example.demo.dtos.requests;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public class ReqMerchantDto {
        @NotBlank(message = "Name is required")
        private String name;

        @NotBlank(message = "Category ID is required")
        private UUID category_id;
        
        @NotBlank(message = "Address is required")
        private String address;
}
