package com.example.demo.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;

@Data
@Schema(name = "ReqRegisterDto", description = "User registration request payload.")
public class ReqRegisterDto {
    @NotBlank(message = "first name is required")
    @Schema(description = "User first name.", example = "Ayu")
    private String firstName;

    @Schema(description = "User last name.", example = "Pratama")
    private String lastName;

    @NotBlank(message = "address is required")
    @Schema(description = "User address.", example = "Jl. Merdeka No. 1, Bandung")
    private String address;

    @NotNull(message = "date of birth is required")
    @Schema(description = "User birth date.", example = "1998-04-12", type = "string", format = "date")
    private LocalDate birthDate;

    @NotBlank(message = "email is required")
    @Email(message = "Please provide a valid email address")
    @Schema(description = "User email address.", example = "ayu@taupay.com")
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Schema(description = "User password with minimum 8 characters.", example = "password123")
    @ToString.Exclude
    private String password;
}
