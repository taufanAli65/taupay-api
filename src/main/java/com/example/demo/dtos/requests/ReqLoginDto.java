package com.example.demo.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

@Data
@Schema(name = "ReqLoginDto", description = "Login request payload.")
public class ReqLoginDto {
    @NotBlank
    @Email
    @Schema(description = "Account email address.", example = "merchant@taupay.com")
    private String email;

    @NotBlank
    @Schema(description = "Account password.", example = "password123")
    @ToString.Exclude
    private String password;
}
