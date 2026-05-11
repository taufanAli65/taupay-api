package com.example.demo.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "ReqMerchantCategoryDto", description = "Merchant category request payload.")
public class ReqMerchantCategoryDto {
    @NotBlank(message = "name is required")
    @Schema(description = "Merchant category name.", example = "Food & Beverage")
    private String name;
}
