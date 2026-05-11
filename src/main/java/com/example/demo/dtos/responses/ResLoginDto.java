package com.example.demo.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ResLoginDto", description = "Authentication response payload.")
public class ResLoginDto {
    @Schema(description = "User profile payload, present when a USER logs in.")
    private ResRegisterDto user;

    @Schema(description = "Merchant profile payload, present when a MERCHANT logs in.")
    private ResRegisterMerchantDto merchant;

    @Schema(description = "JWT bearer token.", example = "eyJhbGciOiJIUzI1NiJ9.example")
    private String token;
}
