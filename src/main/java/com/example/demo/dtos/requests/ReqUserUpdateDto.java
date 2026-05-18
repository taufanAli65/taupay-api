package com.example.demo.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ReqUserUpdateDto", description = "Partial user profile update payload.")
public class ReqUserUpdateDto {
    @Schema(description = "Updated first name.", example = "Ayu")
    private String firstName;

    @Schema(description = "Updated last name.", example = "Pratama")
    private String lastName;

    @Schema(description = "Updated address.", example = "Jl. Asia Afrika No. 5, Bandung")
    private String address;

    @Schema(description = "Updated birth date.", example = "1998-04-12", type = "string", format = "date")
    private LocalDate birthDate;

    @Pattern(regexp = "^\\d{6}$", message = "PIN must be exactly 6 digits")
    @Schema(description = "Updated 6-digit PIN.", example = "123456")
    private String pin;
}
