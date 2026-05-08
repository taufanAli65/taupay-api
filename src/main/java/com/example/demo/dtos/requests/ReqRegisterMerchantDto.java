package com.example.demo.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class ReqRegisterMerchantDto {
    @NotBlank(message = "merchant name is required")
    private String name;

    @NotBlank(message = "address is required")
    private String address;

    @NotNull(message = "category is required")
    private UUID categoryId;

    @NotBlank(message = "email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
}
