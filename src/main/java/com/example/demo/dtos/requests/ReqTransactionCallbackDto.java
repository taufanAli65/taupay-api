package com.example.demo.dtos.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "ReqTransactionCallbackDto", description = "Payment gateway callback payload.")
public class ReqTransactionCallbackDto {
    @JsonProperty("trx_id")
    @NotBlank
    @Schema(description = "Transaction identifier", example = "TRX-12345")
    private String trxId;

    @NotBlank
    @Schema(description = "Payment status", example = "PAID")
    private String status;

    @JsonProperty("payer_user_id")
    @NotBlank
    @Schema(description = "User ID that completed the payment", example = "670f046a-ad29-4380-934d-8f3bb8d7778a")
    private String payerUserId;
}
