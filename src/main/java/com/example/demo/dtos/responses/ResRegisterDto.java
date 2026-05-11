package com.example.demo.dtos.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "ResRegisterDto", description = "User profile payload returned after registration or login.")
public class ResRegisterDto {
    @Schema(description = "User first name.", example = "Ayu")
    private String firstName;

    @Schema(description = "User last name.", example = "Pratama")
    private String lastName;

    @Schema(description = "User email address.", example = "ayu@taupay.com")
    private String email;

    @Schema(description = "User address.", example = "Jl. Merdeka No. 1, Bandung")
    private String address;

    @Schema(description = "User birth date.", example = "1998-04-12")
    private String birthDate;
}
