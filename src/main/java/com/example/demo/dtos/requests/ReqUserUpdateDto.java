package com.example.demo.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqUserUpdateDto {
    private String first_name;
    private String last_name;
    private String address;
    private LocalDate birth_date;
}
