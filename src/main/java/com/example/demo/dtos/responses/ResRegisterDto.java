package com.example.demo.dtos.responses;

import lombok.Data;

@Data
public class ResRegisterDto {
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String birthDate;
}
