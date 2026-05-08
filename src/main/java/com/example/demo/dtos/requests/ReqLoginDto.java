package com.example.demo.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReqLoginDto {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}
