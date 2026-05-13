package com.example.demo.dtos.responses;

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

    @JsonProperty("merchant_id")
    @Schema(description = "ID of the merchant", example = "4e3d11ea-2b75-4c45-bfb7-bbc4f8ab1c29")
    private String merchantId;

    @JsonProperty("created_at")
    @Schema(description = "Timestamp when the transaction was created", example = "2026-05-12T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "List of products purchased")
    private List<ProductItem> products;

    @Schema(description = "Total transaction amount", example = "65000")
    private Long total;

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
    }