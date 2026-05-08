package com.example.demo.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqUserUpdateDto {
    private String firstName;
    private String lastName;
    private String address;
    private LocalDate birthDate;
}
