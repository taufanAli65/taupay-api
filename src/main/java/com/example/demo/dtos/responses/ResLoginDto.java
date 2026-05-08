package com.example.demo.dtos.responses;

import lombok.Data;

@Data
public class ResLoginDto {
    private ResRegisterDto user;
    private String token;
}
