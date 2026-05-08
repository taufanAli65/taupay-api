package com.example.demo.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ReqRegisterDto {
    @NotBlank(message = "first name is required")
    private String firstName;

    private String lastName;

    @NotBlank(message = "address is required")
    private String address;

    @NotNull(message = "date of birth is required")
    private LocalDate birthDate;

    @NotBlank(message = "email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
}
