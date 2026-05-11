package com.example.demo.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "ReqMerchantStatusDto", description = "Merchant activation toggle payload.")
public class ReqMerchantStatusDto {
    @NotNull(message = "Active status is required")
    @Schema(description = "Whether the merchant account is active.", example = "true")
    private Boolean isActive;
}
