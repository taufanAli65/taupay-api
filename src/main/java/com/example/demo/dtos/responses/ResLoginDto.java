package com.example.demo.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResLoginDto {
    private ResRegisterDto user;
    private ResRegisterMerchantDto merchant;
    private String token;
}
