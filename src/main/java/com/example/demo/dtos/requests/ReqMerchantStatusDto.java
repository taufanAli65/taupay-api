package com.example.demo.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqMerchantStatusDto {
    @NotNull(message = "Active status is required")
    private Boolean isActive;
}
