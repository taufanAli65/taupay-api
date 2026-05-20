package com.example.demo.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(name = "ResTransactionDto", description = "Transaction response payload.")
public class ResTransactionDto {

    @JsonProperty("trx_id")
    @Schema(description = "Unique transaction identifier", example = "TRX-12345")
    private String trxId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private MerchantSummary merchant;

    @JsonProperty("created_at")
    @Schema(description = "Timestamp when the transaction was created", example = "2026-05-12T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "List of products purchased")
    private List<ProductItem> products;

    @Schema(description = "Total transaction amount", example = "65000")
    private Long total;

    @Schema(description = "Current transaction status", example = "PENDING")
    private String status = "PENDING";

    @Data
    @Schema(name = "ResTransactionProductItem", description = "Details of a product in the transaction.")
    public static class ProductItem {

        @JsonProperty("product_id")
        @Schema(description = "ID of the product", example = "7902ce2f-6a44-4738-a7fa-6a4d1737cf7f")
        private String productId;

        @Schema(description = "Name of the product", example = "Kopi Gayo V60")
        private String name;

        @Schema(description = "Quantity purchased", example = "2")
        private Integer quantity;

        @Schema(description = "Price per unit", example = "25000")
        private Long price;
    }
    @Data
    @Schema(name = "ResTransactionMerchantSummary", description = "Merchant details included in transaction response.")
    public static class MerchantSummary {

        @JsonProperty("merchant_id")
        @Schema(description = "Merchant identifier.", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
        private String merchantId;

        @JsonProperty("merchant_name")
        @Schema(description = "Merchant name.", example = "Taupay Coffee")
        private String merchantName;

        @JsonProperty("merchant_category_name")
        @Schema(description = "Merchant category name.", example = "Food & Beverage")
        private String merchantCategoryName;

        @JsonProperty("merchant_address")
        @Schema(description = "Merchant address.", example = "Jl. Gatot Subroto No. 20, Jakarta")
        private String merchantAddress;

        @JsonProperty("merchant_is_active")
        @Schema(description = "Whether the merchant account is active.", example = "true")
        private Boolean merchantIsActive;
    }
}