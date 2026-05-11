package com.example.demo.dtos.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ResRegisterMerchantDto", description = "Merchant payload returned after registration or login.")
public class ResRegisterMerchantDto {
    @Schema(description = "Merchant identifier.", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    private UUID id;

    @Schema(description = "Merchant name.", example = "Taupay Coffee")
    private String name;

    @Schema(description = "Merchant account email address.", example = "owner@taupaycoffee.com")
    private String email;

    @Schema(description = "Merchant address.", example = "Jl. Gatot Subroto No. 20, Jakarta")
    private String address;

    @Schema(description = "Merchant category name.", example = "Food & Beverage")
    private String category;
}
