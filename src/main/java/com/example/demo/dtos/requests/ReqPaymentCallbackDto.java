package com.example.demo.dtos.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(name = "ReqPaymentCallbackDto", description = "Payment callback request payload.")
public class ReqPaymentCallbackDto {

    @NotBlank(message = "trx_id is required")
    @JsonProperty("trx_id")
    @Schema(description = "Transaction identifier.", example = "TRX-1234ABCD")
    private String trxId;

    @NotBlank(message = "pin is required")
    @Pattern(regexp = "^\\d{6}$", message = "PIN must be exactly 6 digits")
    @Schema(description = "6-digit account PIN.", example = "123456")
    private String pin;

    @Schema(description = "Payment status (e.g., PAID).", example = "PAID")
    private String status;
}
