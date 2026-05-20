package com.example.demo.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

import java.util.UUID;

@Data
@Schema(name = "ReqRegisterMerchantDto", description = "Merchant registration request payload.")
public class ReqRegisterMerchantDto {
    @NotBlank(message = "merchant name is required")
    @Schema(description = "Merchant name.", example = "Taupay Coffee")
    private String name;

    @NotBlank(message = "address is required")
    @Schema(description = "Merchant address.", example = "Jl. Gatot Subroto No. 20, Jakarta")
    private String address;

    @NotNull(message = "category is required")
    @Schema(description = "Merchant category identifier.", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    private UUID categoryId;

    @NotBlank(message = "email is required")
    @Email(message = "Please provide a valid email address")
    @Schema(description = "Merchant account email address.", example = "owner@taupaycoffee.com")
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Schema(description = "Merchant account password with minimum 8 characters.", example = "password123")
    @ToString.Exclude
    private String password;
}
