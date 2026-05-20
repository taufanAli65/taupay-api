package com.example.demo.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.ToString;

@Data
@Schema(name = "ReqChangePinDto", description = "Payload for changing or setting a new PIN.")
public class ReqChangePinDto {
    
    @Pattern(regexp = "^\\d{6}$", message = "Old PIN must be exactly 6 digits")
    @Schema(description = "The current 6-digit PIN. Optional only if the user hasn't set a PIN yet.", example = "123456")
    @ToString.Exclude
    private String oldPin;

    @NotBlank(message = "New PIN is required")
    @Pattern(regexp = "^\\d{6}$", message = "New PIN must be exactly 6 digits")
    @Schema(description = "The new 6-digit PIN.", example = "654321")
    @ToString.Exclude
    private String newPin;
}
