package com.example.demo.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(name = "ReqMerchantDto", description = "Merchant update request payload.")
public class ReqMerchantDto {
    @NotBlank(message = "Name is required")
    @Schema(description = "Merchant display name.", example = "Taupay Coffee")
    private String name;

    @NotNull(message = "Category ID is required")
    @Schema(description = "Merchant category identifier.", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    private UUID categoryId;

    @NotBlank(message = "Address is required")
    @Schema(description = "Merchant address.", example = "Jl. Sudirman No. 10, Jakarta")
    private String address;

    @Pattern(regexp = "^\\d{6}$", message = "PIN must be exactly 6 digits")
    @Schema(description = "Updated 6-digit PIN.", example = "123456")
    private String pin;
}
