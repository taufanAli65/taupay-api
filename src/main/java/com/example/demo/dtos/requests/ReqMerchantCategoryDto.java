package com.example.demo.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReqMerchantCategoryDto {
    @NotBlank(message = "name is required")
    private String name;
}
